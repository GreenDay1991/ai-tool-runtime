package org.example.aitoolruntime.springai.security;

import org.springframework.ai.chat.model.ToolContext;

/**
 * 工具安全执行边界 SPI。
 *
 * <p>每个 {@code SecuredToolCallback} 在真正调用目标业务方法之前，都会调用
 * {@link #check(ToolContext)} 完成：身份认证、授权、Agent 调用链检查与最大嵌套深度检查。
 * 校验失败必须抛出 {@link ToolSecurityException}，并确保目标业务方法不被执行。</p>
 */
public interface ToolAuthorizationManager {

    /**
     * 执行安全检查，失败时抛出 {@link ToolSecurityException}。
     *
     * @param toolContext 工具执行上下文（可能为 {@code null}，此时应视为缺少身份而拒绝）
     */
    void check(ToolContext toolContext);
}
