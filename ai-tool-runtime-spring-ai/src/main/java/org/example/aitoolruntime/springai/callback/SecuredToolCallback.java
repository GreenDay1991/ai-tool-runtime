package org.example.aitoolruntime.springai.callback;

import java.util.Objects;

import org.example.aitoolruntime.springai.security.RuntimeApiPolicy;
import org.example.aitoolruntime.springai.security.ToolAuthorizationManager;
import org.example.aitoolruntime.springai.security.ToolContextSupport;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

/**
 * Runtime 的安全执行边界。
 *
 * <p>持有内部委托（通常为 {@code MethodToolCallback}）与授权管理器。每次调用前先完成
 * 身份认证 / 授权 / 调用链 / 最大深度检查，通过后才把调用转发给内部委托（进而打到
 * 真实的 Spring Bean 代理）。</p>
 *
 * <p>该类不可变、不提供关闭或绕过鉴权的 API、不暴露内部委托。</p>
 */
public final class SecuredToolCallback implements ToolCallback {

    private final ToolCallback delegate;
    private final ToolAuthorizationManager authorizationManager;
    private final RuntimeApiPolicy runtimeApiPolicy;
    private final boolean runtimeApiTool;

    public SecuredToolCallback(ToolCallback delegate, ToolAuthorizationManager authorizationManager,
            RuntimeApiPolicy runtimeApiPolicy, boolean runtimeApiTool) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.authorizationManager = Objects.requireNonNull(authorizationManager, "authorizationManager");
        this.runtimeApiPolicy = runtimeApiPolicy;
        this.runtimeApiTool = runtimeApiTool;
    }

    @Override
    public ToolDefinition getToolDefinition() {
        return delegate.getToolDefinition();
    }

    @Override
    public ToolMetadata getToolMetadata() {
        return delegate.getToolMetadata();
    }

    @Override
    public String call(String toolInput) {
        return call(toolInput, null);
    }

    @Override
    public String call(String toolInput, ToolContext toolContext) {
        if (runtimeApiPolicy != null) {
            runtimeApiPolicy.check(runtimeApiTool);
        }
        authorizationManager.check(toolContext);
        return delegate.call(toolInput, extendChain(toolContext));
    }

    private ToolContext extendChain(ToolContext toolContext) {
        String agentId = ToolContextSupport.resolveAgentId(toolContext);
        if (agentId == null) {
            return toolContext;
        }
        return ToolContextSupport.appendAgent(toolContext, agentId);
    }
}
