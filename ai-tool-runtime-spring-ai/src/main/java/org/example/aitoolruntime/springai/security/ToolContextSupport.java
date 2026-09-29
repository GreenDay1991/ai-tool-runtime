package org.example.aitoolruntime.springai.security;

import java.util.HashMap;
import java.util.Map;

import org.springframework.ai.chat.model.ToolContext;

/**
 * {@code ToolContext} 的读取与调用链传递工具方法，供鉴权与回调共用。
 */
public final class ToolContextSupport {

    private ToolContextSupport() {
    }

    /** 从上下文中读取 Agent 身份，缺失返回 {@code null}。 */
    public static String resolveAgentId(ToolContext toolContext) {
        if (toolContext == null) {
            return null;
        }
        Object value = toolContext.getContext().get(ToolContextKeys.AGENT_ID);
        return value == null ? null : String.valueOf(value);
    }

    /** 从上下文中读取 Agent 调用链，缺失返回空链。 */
    public static AgentInvocationChain resolveChain(ToolContext toolContext) {
        if (toolContext == null) {
            return AgentInvocationChain.empty();
        }
        Object value = toolContext.getContext().get(ToolContextKeys.AGENT_INVOCATION_CHAIN);
        return value instanceof AgentInvocationChain chain ? chain : AgentInvocationChain.empty();
    }

    /**
     * 构造一个新上下文，将当前 Agent 追加到调用链尾（用于向嵌套调用传递）。
     */
    public static ToolContext appendAgent(ToolContext toolContext, String agentId) {
        Map<String, Object> next = new HashMap<>(toolContext != null ? toolContext.getContext() : Map.of());
        next.put(ToolContextKeys.AGENT_INVOCATION_CHAIN, resolveChain(toolContext).append(agentId));
        return new ToolContext(next);
    }
}
