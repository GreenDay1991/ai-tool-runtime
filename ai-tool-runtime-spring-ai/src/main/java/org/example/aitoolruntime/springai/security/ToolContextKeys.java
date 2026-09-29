package org.example.aitoolruntime.springai.security;

/**
 * Runtime 在 Spring AI {@code ToolContext} 中使用的约定键名。
 *
 * <p>调用方负责向 {@code ToolContext} 提供可信鉴权信息（例如从登录态取数后注入），
 * Runtime 只负责读取与校验，绝不信任 LLM 自行生成的身份信息。</p>
 */
public final class ToolContextKeys {

    /** 当前 Agent 身份的键，值为字符串（如 userId / agentId / tenantId 等宿主约定的标识）。 */
    public static final String AGENT_ID = "ai.tool-runtime.agent.id";

    /** Agent 调用链的键，值为 {@link AgentInvocationChain} 实例。 */
    public static final String AGENT_INVOCATION_CHAIN = "ai.tool-runtime.agent.invocation-chain";

    private ToolContextKeys() {
    }
}
