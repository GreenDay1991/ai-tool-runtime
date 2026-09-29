# ai-tool-runtime

Spring AI Tool 的**动态运行时增强层**。

在 Spring AI 原生 Tool 体系之上，提供业务方法的动态发现、稳定方法标识、`MethodToolCallback` 动态创建，以及对所有工具**强制安全包装**（Agent 身份认证 / 授权 / 调用链防套娃 / 最大嵌套深度）。

## 项目定位

| 能力 | 说明 |
| --- | --- |
| 动态发现 | 发现符合条件的 Spring Bean 公开业务方法 |
| 稳定标识 | 为方法生成稳定的 `ToolMethodId`（可区分 Bean / 方法 / 重载） |
| 动态创建 | 动态创建 Spring AI 原生 `MethodToolCallback` |
| 安全包装 | 对 Tool 强制增加 `SecuredToolCallback` |
| 身份与授权 | 支持 Agent 身份认证、授权与调用链检测 |
| 注册 | 支持单个 / 批量 Tool 注册 |
| 动态定义 | 支持动态 Tool Definition |
| Runtime API 化 | 支持可配置的 Runtime API Tool 化 |

最终 Tool 兼容 Spring AI 原生：`ToolCallback` / `MethodToolCallback` / `ToolDefinition` / `ToolContext`。

### 明确不做的（边界）

- 不使用 Semantic Kernel，不实现自己的 Tool 标准
- 不新增自定义 `@Tool` 注解，不替代 Spring AI `@Tool`
- 不实现 Agent Loop / 聊天系统 / 自定义 Agent 框架
- 不实现业务权限管理系统（只定义「执行前必须安全检查」，具体权限由宿主提供）
- 不阻止开发者主动绕过 Runtime 自行创建 Tool

## 模块结构

```
ai-tool-runtime/
├── pom.xml                        # 父 POM（管理模块与 Spring AI BOM）
├── ai-tool-runtime-core/          # 纯 Java 模型与 API（零 Spring / Spring AI 依赖）
│   └── core/
│       ├── model/     ScanScope / ToolMethodDescriptor / ToolMethodParameter / ToolMethodId
│       ├── exception/ ToolRuntimeException / ToolMethodNotFoundException
│       └── api/       ToolMethodDiscovery
├── ai-tool-runtime-spring/        # Spring Bean / Method 发现与解析（不依赖 Spring AI）
│   └── spring/
│       ├── discovery/ SpringBeanScanner / SpringBeanFilter / SpringMethodScanner / DefaultToolMethodDiscovery
│       ├── resolver/  SpringBeanResolver / SpringMethodResolver
│       └── config/    AiToolRuntimeSpringConfiguration
└── ai-tool-runtime-spring-ai/     # Spring AI Tool 创建、安全包装与运行时
    └── springai/
        ├── factory/   MethodToolCallbackFactory / SecuredToolCallbackFactory / ToolInputSchemaGenerator
        ├── callback/  SecuredToolCallback
        ├── security/  ToolAuthorization / ToolAuthorizationManager / DefaultToolAuthorizationManager
        │              AgentInvocationChain / RuntimeApiPolicy / ToolContextKeys / ToolSecurityException
        ├── runtime/   AiToolRuntime / AiToolRuntimeImpl / ToolRegistration
        └── config/    AiToolRuntimeAutoConfiguration / AiToolRuntimeProperties
```

## 快速开始

引入 `ai-tool-runtime-spring-ai` 后，自动装配会提供 `AiToolRuntime` Bean（无需额外配置）：

```java
@Autowired
AiToolRuntime runtime;

// 1. 发现（默认只纳入 @Service / @Repository，排除 @Tool 方法）
List<ToolMethodDescriptor> methods = runtime.discover(ScanScope.defaults());

// 2. 创建安全工具
ToolCallback tool = runtime.createTool(methods.get(0).getId(), "myTool", "工具描述");

// 3. 调用（ToolContext 中携带可信 Agent 身份）
ToolContext context = new ToolContext(Map.of(ToolContextKeys.AGENT_ID, "user-123"));
String result = tool.call("{\"arg\":\"value\"}", context);
```

默认发现规则：

- **纳入**：`@Service`、`@Repository` 标注的 Bean。
- **排除**：`@Controller`、`@RestController`、`@Configuration`、普通 `@Component`、基础设施 Bean、Util / 普通 Java 类。
- **方法层面**：已标记 Spring AI `@Tool` 的方法直接排除（交给 Spring AI 原生处理，不重复注册）。

## 核心 API

```java
public interface AiToolRuntime {
    List<ToolMethodDescriptor> discover(ScanScope scope);
    ToolCallback createTool(ToolMethodId methodId, String name, String description);
    ToolCallback register(ToolMethodId methodId, String name, String description);
    List<ToolCallback> registerBatch(List<ToolRegistration> registrations);
}
```

- 单个 / 批量注册都最终经过 `MethodToolCallback → SecuredToolCallback`。
- Runtime 对外**只返回**经 `SecuredToolCallback` 包装的 Tool，不提供无鉴权创建接口。

## 安全模型

最终执行模型：

```
Spring AI Agent → ToolCallback → SecuredToolCallback → ToolAuthorizationManager
    → 身份认证 → 权限校验 → 调用链检查 → maxDepth 检查
    → MethodToolCallback → ApplicationContext → 真实 Bean Proxy → 业务方法
```

关键点：

1. **强制包装**：`SecuredToolCallback` 是不可变的安全边界，不提供关闭/绕过鉴权 API，不暴露内部 delegate。
2. **身份认证**：Agent 身份通过 `ToolContext` 传入（键 `ai.tool-runtime.agent.id`）。缺失即拒绝；**绝不信任 LLM 生成的身份信息**。
3. **授权解耦**：`ToolAuthorizationManager` / `ToolAuthorization` 是宿主扩展点。默认放行（仅保证身份存在 + 无循环 + 未超深度），生产环境应覆盖为具体权限实现。
4. **防套娃**：维护 Agent 调用链 `A → B → C`，检测到重复 Agent（如 `A → B → A`）即拒绝。
5. **最大深度**：超过 `maxDepth` 即拒绝。
6. **鉴权失败不执行**：任何校验失败都会抛出 `ToolSecurityException`，目标业务方法绝不会被调用。

调用链由 Runtime 维护并随 `ToolContext` 传递，不依赖 LLM 自行维护。

## 配置

```yaml
ai:
  tool-runtime:
    max-depth: 10                        # Agent 调用链最大嵌套深度，默认 10
    runtime-api:
      allow-tool-execution: false        # 是否允许 Runtime API 被 Tool 化并在 Tool 上下文执行，默认 false
```

- 默认关闭时，Runtime API（`discover` / `register` / `registerBatch`）只是普通 Java API；若被注册为 Tool 并在 Tool 执行上下文中调用，会被拒绝。
- 开启后，可主动将 Runtime API 注册为 Tool（走与普通业务工具完全相同的安全包装）：

```java
ToolRegistration.runtimeApi(methodId, "discover", "发现工具方法")
```

## 构建与测试

```bash
./mvnw clean verify        # 编译 + 运行全部测试
./mvnw clean install       # 安装到本地仓库
```

环境要求：JDK 17+（项目 `java.version=17`），Maven 3.9+（使用内置 Maven Wrapper）。

## 测试覆盖

- Bean 方法发现（`@Service` / `@Repository` 纳入，`@Controller` / `@Component` / `@Configuration` 排除）
- `@Tool` 方法排除
- 重载方法 ID 唯一性
- Spring 真实 Bean（含代理）获取
- Tool 创建
- 鉴权成功 / 失败、鉴权失败时目标方法不执行
- Agent 调用链传递、循环调用检测、maxDepth 检测
- Runtime API 默认禁止 Tool 上下文调用、开启后的安全包装

详细设计见 [DESIGN.md](DESIGN.md)。
