# 设计说明

本文面向后续维护者，说明 ai-tool-runtime 的职责、API 与安全模型。

## 1. 分层与模块边界

| 模块 | 职责 | 依赖约束 |
| --- | --- | --- |
| `ai-tool-runtime-core` | 纯 Java 模型与 API（`ScanScope`、`ToolMethodDescriptor`、`ToolMethodId`、`ToolMethodDiscovery`） | 零依赖（不依赖 Spring / Spring AI / Spring Security） |
| `ai-tool-runtime-spring` | `ScanScope → ApplicationContext → Bean 发现 → Method 发现 → ToolMethodDescriptor`；Bean 解析、Method 解析 | 仅依赖 Spring；不依赖 Spring AI |
| `ai-tool-runtime-spring-ai` | Tool 创建、`SecuredToolCallback`、身份 / 授权 / 调用链、Runtime API Tool 化 | 依赖 Spring AI + Spring Boot 自动装配 |

跨模块引用只依赖 `core` 的模型与接口；Spring AI 类型只在 `spring-ai` 模块出现。

## 2. 方法动态发现

- `ScanScope` 以**注解全限定名（字符串）**描述扫描范围，避免 core 依赖 Spring / Spring AI。
- 默认纳入 `@Service` / `@Repository`；默认排除 `@Controller` / `@RestController` / `@Configuration` / 普通 `@Component` / 基础设施 Bean。
- 方法层面按名排除 `Object` 方法与 Spring 生命周期方法；按全限定名排除 Spring AI `@Tool`（默认 `org.springframework.ai.tool.annotation.Tool`），从而与原生 Tool 互不重复。

筛选顺序（`SpringBeanFilter`）：按名排除 → 按包过滤 → 命中纳入注解即纳入 → 命中排除注解即排除 → 其余一律排除。

## 3. 稳定方法标识

`ToolMethodId = beanName + methodName + parameterTypes[]`，仅由可反射获得的结构化信息计算，不依赖运行时随机值，可稳定区分：

- 不同 Bean；
- 不同方法；
- 同名方法的重载（参数类型不同）。

规范字符串形式：`beanName#methodName(paramType1,paramType2)`，支持反向解析。

## 4. Spring Bean 获取（保留代理能力）

创建 Tool 时**必须**通过 `ApplicationContext` 获取真实 Bean：

```
ToolMethodDescriptor → ApplicationContext → 真实 Spring Bean → Method → MethodToolCallback
```

禁止 `new Service()`。真实 Bean 可能是 JDK 动态代理或 CGLIB 代理；`SpringMethodResolver` 通过 `AopUtils.getTargetClass` 穿透代理解析方法，方法再在代理对象上调用，从而保留 Spring AOP、`@Transactional`、Spring Security 等能力。

## 5. Tool 创建与强制安全包装

```
ToolMethodDescriptor + 真实 Bean + Method + ToolDefinition
    → MethodToolCallback → SecuredToolCallback → ToolCallback
```

- `MethodToolCallbackFactory` 只创建原始回调，属于内部能力，其公开方法返回 `ToolCallback`（不暴露具体实现）。
- Runtime 对外只返回经 `SecuredToolCallbackFactory` 包装后的 Tool；不存在无鉴权创建入口。

## 6. SecuredToolCallback（安全执行边界）

`public final class SecuredToolCallback implements ToolCallback`，内部持有 `delegate` 与 `authorizationManager`。特点：

- 不可变（final 字段）；
- 无关闭 / 绕过鉴权 API；
- 不暴露内部 delegate；
- 每次 `call` 先鉴权，通过后才转发给 delegate（进而命中真实 Bean 代理）。

## 7. 身份、授权与调用链

- 身份经 `ToolContext` 传递（键 `ai.tool-runtime.agent.id`）；调用方负责注入**可信**鉴权信息，Runtime 不规定 Token 获取方式，也不信任 LLM 生成的身份。
- `ToolAuthorizationManager.check(ToolContext)` 是安全总闸，默认实现依次：
  1. 身份认证（缺失拒绝）；
  2. 循环调用检测（当前 Agent 已在链中拒绝）；
  3. 最大深度检测（链深度 ≥ `maxDepth` 拒绝）；
  4. 授权（委托 `ToolAuthorization`，默认放行，宿主覆盖）。
- 调用链 `AgentInvocationChain` 不可变，随 `ToolContext` 传递，由 Runtime 维护。每次安全调用会把当前 Agent 追加到链尾并传入委托，供嵌套调用继续传递。

## 8. Runtime API Tool 化策略

- 默认 `ai.tool-runtime.runtime-api.allow-tool-execution=false`：Runtime API 在 Tool 执行上下文中被调用即拒绝。
- 开启后可主动注册 Runtime API 为 Tool，且走与业务工具完全相同的安全包装（`securedRuntimeApi` 标记受 `RuntimeApiPolicy` 控制）。

## 9. 边界与不扩展项

Runtime 只对自己创建并返回的 Tool 负责，不接管整个 Spring AI Tool 体系，也不阻止开发者自行绕过。不分析 Controller → Service / Service → Mapper 调用链，不自动暴露 Mapper / DAO，不实现 Agent Loop、聊天系统、Agent 生命周期或业务权限系统。
