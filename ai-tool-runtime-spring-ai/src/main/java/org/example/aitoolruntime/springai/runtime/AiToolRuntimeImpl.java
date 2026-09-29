package org.example.aitoolruntime.springai.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.example.aitoolruntime.core.api.ToolMethodDiscovery;
import org.example.aitoolruntime.core.exception.ToolMethodNotFoundException;
import org.example.aitoolruntime.core.model.ScanScope;
import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.example.aitoolruntime.core.model.ToolMethodId;
import org.example.aitoolruntime.springai.factory.MethodToolCallbackFactory;
import org.example.aitoolruntime.springai.factory.SecuredToolCallbackFactory;
import org.springframework.ai.tool.ToolCallback;

/**
 * {@link AiToolRuntime} 的默认实现。
 *
 * <p>维护「方法标识 → 描述符」的注册表：{@link #discover(ScanScope)} 会填充注册表，
 * 后续的 {@link #createTool} / {@link #register} / {@link #registerBatch} 依据标识查表创建工具。</p>
 */
public class AiToolRuntimeImpl implements AiToolRuntime {

    private final ToolMethodDiscovery discovery;
    private final MethodToolCallbackFactory methodToolCallbackFactory;
    private final SecuredToolCallbackFactory securedToolCallbackFactory;
    private final Map<ToolMethodId, ToolMethodDescriptor> registry = new ConcurrentHashMap<>();

    public AiToolRuntimeImpl(ToolMethodDiscovery discovery,
            MethodToolCallbackFactory methodToolCallbackFactory,
            SecuredToolCallbackFactory securedToolCallbackFactory) {
        this.discovery = Objects.requireNonNull(discovery, "discovery");
        this.methodToolCallbackFactory = Objects.requireNonNull(methodToolCallbackFactory, "methodToolCallbackFactory");
        this.securedToolCallbackFactory = Objects.requireNonNull(securedToolCallbackFactory, "securedToolCallbackFactory");
    }

    @Override
    public List<ToolMethodDescriptor> discover(ScanScope scope) {
        List<ToolMethodDescriptor> descriptors = discovery.discover(scope);
        for (ToolMethodDescriptor descriptor : descriptors) {
            registry.put(descriptor.getId(), descriptor);
        }
        return List.copyOf(descriptors);
    }

    @Override
    public ToolCallback createTool(ToolMethodId methodId, String name, String description) {
        ToolMethodDescriptor descriptor = requireDescriptor(methodId);
        ToolCallback methodCallback = methodToolCallbackFactory.create(descriptor, name, description);
        return securedToolCallbackFactory.secured(methodCallback);
    }

    @Override
    public ToolCallback register(ToolMethodId methodId, String name, String description) {
        return createTool(methodId, name, description);
    }

    @Override
    public List<ToolCallback> registerBatch(List<ToolRegistration> registrations) {
        List<ToolCallback> tools = new ArrayList<>();
        for (ToolRegistration registration : registrations) {
            ToolMethodDescriptor descriptor = requireDescriptor(registration.methodId());
            ToolCallback methodCallback = methodToolCallbackFactory.create(descriptor, registration.name(),
                    registration.description());
            ToolCallback secured = registration.runtimeApi()
                    ? securedToolCallbackFactory.securedRuntimeApi(methodCallback)
                    : securedToolCallbackFactory.secured(methodCallback);
            tools.add(secured);
        }
        return List.copyOf(tools);
    }

    private ToolMethodDescriptor requireDescriptor(ToolMethodId methodId) {
        ToolMethodDescriptor descriptor = registry.get(methodId);
        if (descriptor == null) {
            throw new ToolMethodNotFoundException(methodId);
        }
        return descriptor;
    }
}
