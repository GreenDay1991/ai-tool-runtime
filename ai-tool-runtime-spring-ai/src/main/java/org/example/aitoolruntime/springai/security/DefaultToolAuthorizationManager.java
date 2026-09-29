package org.example.aitoolruntime.springai.security;

import org.springframework.ai.chat.model.ToolContext;

/**
 * {@link ToolAuthorizationManager} 的默认实现。
 *
 * <p>依次执行：</p>
 * <ol>
 *     <li>身份认证：从 {@code ToolContext} 读取 Agent 身份，缺失即拒绝；</li>
 *     <li>循环调用检测：当前 Agent 已存在于调用链中即拒绝；</li>
 *     <li>最大嵌套深度检测：链深度达到 {@code maxDepth} 即拒绝；</li>
 *     <li>授权：委托 {@link ToolAuthorization} 决策（默认放行，可由宿主覆盖）。</li>
 * </ol>
 */
public class DefaultToolAuthorizationManager implements ToolAuthorizationManager {

    private final ToolAuthorization authorization;
    private final int maxDepth;

    public DefaultToolAuthorizationManager(ToolAuthorization authorization, int maxDepth) {
        this.authorization = authorization;
        this.maxDepth = maxDepth;
    }

    @Override
    public void check(ToolContext toolContext) {
        String agentId = ToolContextSupport.resolveAgentId(toolContext);
        if (agentId == null || agentId.isBlank()) {
            throw new ToolSecurityException("Agent identity is required to execute a secured tool");
        }

        AgentInvocationChain chain = ToolContextSupport.resolveChain(toolContext);
        if (chain.contains(agentId)) {
            throw new ToolSecurityException("Agent invocation loop detected for agent: " + agentId);
        }
        if (chain.depth() >= maxDepth) {
            throw new ToolSecurityException("Maximum agent invocation depth exceeded: " + maxDepth);
        }

        if (authorization != null && !authorization.authorize(toolContext)) {
            throw new ToolSecurityException("Agent is not authorized to execute this tool: " + agentId);
        }
    }
}
