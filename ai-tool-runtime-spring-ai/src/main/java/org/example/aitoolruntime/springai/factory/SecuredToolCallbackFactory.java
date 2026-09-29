package org.example.aitoolruntime.springai.factory;

import java.util.Objects;

import org.example.aitoolruntime.springai.callback.SecuredToolCallback;
import org.example.aitoolruntime.springai.security.RuntimeApiPolicy;
import org.example.aitoolruntime.springai.security.ToolAuthorizationManager;
import org.springframework.ai.tool.ToolCallback;

/**
 * 将原始 {@link ToolCallback} 包装为 {@link SecuredToolCallback} 的工厂。
 *
 * <p>Runtime 对外只返回经此工厂包装后的安全 Tool；单工具与批量注册都必须经过该包装。</p>
 */
public class SecuredToolCallbackFactory {

    private final ToolAuthorizationManager authorizationManager;
    private final RuntimeApiPolicy runtimeApiPolicy;

    public SecuredToolCallbackFactory(ToolAuthorizationManager authorizationManager,
            RuntimeApiPolicy runtimeApiPolicy) {
        this.authorizationManager = Objects.requireNonNull(authorizationManager, "authorizationManager");
        this.runtimeApiPolicy = Objects.requireNonNull(runtimeApiPolicy, "runtimeApiPolicy");
    }

    /** 包装普通业务工具。 */
    public SecuredToolCallback secured(ToolCallback delegate) {
        return new SecuredToolCallback(delegate, authorizationManager, runtimeApiPolicy, false);
    }

    /** 包装 Runtime API 工具（受 {@code allow-tool-execution} 控制）。 */
    public SecuredToolCallback securedRuntimeApi(ToolCallback delegate) {
        return new SecuredToolCallback(delegate, authorizationManager, runtimeApiPolicy, true);
    }
}
