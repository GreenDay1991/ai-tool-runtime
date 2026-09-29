package org.example.aitoolruntime.springai.runtime;

import java.util.List;

import org.example.aitoolruntime.core.model.ScanScope;
import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.example.aitoolruntime.core.model.ToolMethodId;
import org.springframework.ai.tool.ToolCallback;

/**
 * ai-tool-runtime 对外核心入口。
 *
 * <p>所有创建 / 注册方法最终都返回经 {@code SecuredToolCallback} 包装后的
 * {@link ToolCallback}，不提供无鉴权工具的创建入口。</p>
 */
public interface AiToolRuntime {

    /**
     * 在指定范围内发现候选业务方法。
     */
    List<ToolMethodDescriptor> discover(ScanScope scope);

    /**
     * 依据方法标识创建（并注册）一个安全工具。
     *
     * @param methodId    由 {@link #discover(ScanScope)} 返回的方法标识
     * @param name        工具名
     * @param description 工具描述
     */
    ToolCallback createTool(ToolMethodId methodId, String name, String description);

    /**
     * 注册单个工具（等价于 {@link #createTool}）。
     */
    ToolCallback register(ToolMethodId methodId, String name, String description);

    /**
     * 批量注册工具。
     */
    List<ToolCallback> registerBatch(List<ToolRegistration> registrations);
}
