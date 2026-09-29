package org.example.aitoolruntime.springai.security;

import org.springframework.ai.chat.model.ToolContext;

/**
 * 授权决策 SPI。由宿主应用提供，用于判断当前 Agent 是否被允许调用工具。
 *
 * <p>Runtime 只定义「工具执行前必须经过安全检查」，具体的 Token 来源、用户权限系统、
 * 业务角色系统均由宿主应用实现，Runtime 不做业务权限管理。</p>
 */
@FunctionalInterface
public interface ToolAuthorization {

    /**
     * 判断当前调用是否被授权。
     *
     * @param toolContext 工具执行上下文（包含 {@link ToolContextKeys#AGENT_ID} 等）
     * @return {@code true} 表示允许执行；{@code false} 将被拒绝
     */
    boolean authorize(ToolContext toolContext);
}
