package org.example.aitoolruntime.springai.callback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.example.aitoolruntime.springai.security.AgentInvocationChain;
import org.example.aitoolruntime.springai.security.DefaultToolAuthorizationManager;
import org.example.aitoolruntime.springai.security.RuntimeApiPolicy;
import org.example.aitoolruntime.springai.security.ToolAuthorizationManager;
import org.example.aitoolruntime.springai.security.ToolContextKeys;
import org.example.aitoolruntime.springai.security.ToolSecurityException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.metadata.ToolMetadata;

class SecuredToolCallbackTest {

    private final RecordingDelegate delegate = new RecordingDelegate("ok");
    private final ToolAuthorizationManager permitAll = new DefaultToolAuthorizationManager(toolContext -> true, 10);

    @Test
    void invokesDelegateWhenAuthorized() {
        SecuredToolCallback callback = secured(false, null);
        ToolContext context = new ToolContext(Map.<String, Object>of(ToolContextKeys.AGENT_ID, "agent-a"));

        assertThat(callback.call("{}", context)).isEqualTo("ok");
        assertThat(delegate.invocations.get()).isEqualTo(1);
    }

    @Test
    void rejectsWhenNoAgentIdentity() {
        SecuredToolCallback callback = secured(false, null);

        assertThatThrownBy(() -> callback.call("{}", new ToolContext(Map.of())))
                .isInstanceOf(ToolSecurityException.class);
        assertThat(delegate.invocations.get()).isZero();
    }

    @Test
    void rejectsWhenContextIsNull() {
        SecuredToolCallback callback = secured(false, null);

        assertThatThrownBy(() -> callback.call("{}", null))
                .isInstanceOf(ToolSecurityException.class);
        assertThat(delegate.invocations.get()).isZero();
    }

    @Test
    void rejectsWhenUnauthorized() {
        ToolAuthorizationManager denyAll = new DefaultToolAuthorizationManager(toolContext -> false, 10);
        SecuredToolCallback callback = new SecuredToolCallback(delegate, denyAll, null, false);
        ToolContext context = new ToolContext(Map.<String, Object>of(ToolContextKeys.AGENT_ID, "agent-a"));

        assertThatThrownBy(() -> callback.call("{}", context))
                .isInstanceOf(ToolSecurityException.class);
        assertThat(delegate.invocations.get()).isZero();
    }

    @Test
    void rejectsOnAgentInvocationLoop() {
        ToolContext context = new ToolContext(Map.of(
                ToolContextKeys.AGENT_ID, "agent-a",
                ToolContextKeys.AGENT_INVOCATION_CHAIN, AgentInvocationChain.of(List.of("agent-b", "agent-a"))));

        assertThatThrownBy(() -> secured(false, null).call("{}", context))
                .isInstanceOf(ToolSecurityException.class);
        assertThat(delegate.invocations.get()).isZero();
    }

    @Test
    void rejectsWhenMaxDepthExceeded() {
        ToolAuthorizationManager manager = new DefaultToolAuthorizationManager(toolContext -> true, 2);
        SecuredToolCallback callback = new SecuredToolCallback(delegate, manager, null, false);
        ToolContext context = new ToolContext(Map.of(
                ToolContextKeys.AGENT_ID, "agent-c",
                ToolContextKeys.AGENT_INVOCATION_CHAIN, AgentInvocationChain.of(List.of("agent-a", "agent-b"))));

        assertThatThrownBy(() -> callback.call("{}", context))
                .isInstanceOf(ToolSecurityException.class);
        assertThat(delegate.invocations.get()).isZero();
    }

    @Test
    void propagatesExtendedChainToDelegate() {
        SecuredToolCallback callback = secured(false, null);
        ToolContext context = new ToolContext(Map.<String, Object>of(ToolContextKeys.AGENT_ID, "agent-a"));

        callback.call("{}", context);

        ToolContext received = delegate.lastContext.get();
        AgentInvocationChain chain = (AgentInvocationChain) received.getContext()
                .get(ToolContextKeys.AGENT_INVOCATION_CHAIN);
        assertThat(chain.agents()).containsExactly("agent-a");
    }

    @Test
    void runtimeApiToolRejectedWhenDisabled() {
        SecuredToolCallback callback = secured(true, new RuntimeApiPolicy(false));
        ToolContext context = new ToolContext(Map.<String, Object>of(ToolContextKeys.AGENT_ID, "agent-a"));

        assertThatThrownBy(() -> callback.call("{}", context))
                .isInstanceOf(ToolSecurityException.class);
        assertThat(delegate.invocations.get()).isZero();
    }

    @Test
    void runtimeApiToolAllowedWhenEnabled() {
        SecuredToolCallback callback = secured(true, new RuntimeApiPolicy(true));
        ToolContext context = new ToolContext(Map.<String, Object>of(ToolContextKeys.AGENT_ID, "agent-a"));

        assertThat(callback.call("{}", context)).isEqualTo("ok");
        assertThat(delegate.invocations.get()).isEqualTo(1);
    }

    private SecuredToolCallback secured(boolean runtimeApiTool, RuntimeApiPolicy policy) {
        return new SecuredToolCallback(delegate, permitAll, policy, runtimeApiTool);
    }

    /** 记录调用次数的委托桩。 */
    private static final class RecordingDelegate implements ToolCallback {

        private final AtomicInteger invocations = new AtomicInteger();
        private final AtomicReference<ToolContext> lastContext = new AtomicReference<>();
        private final String result;

        RecordingDelegate(String result) {
            this.result = result;
        }

        @Override
        public ToolDefinition getToolDefinition() {
            return null;
        }

        @Override
        public ToolMetadata getToolMetadata() {
            return null;
        }

        @Override
        public String call(String toolInput) {
            return call(toolInput, null);
        }

        @Override
        public String call(String toolInput, ToolContext toolContext) {
            invocations.incrementAndGet();
            lastContext.set(toolContext);
            return result;
        }
    }
}
