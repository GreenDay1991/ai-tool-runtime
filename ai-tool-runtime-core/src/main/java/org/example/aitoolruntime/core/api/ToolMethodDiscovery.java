package org.example.aitoolruntime.core.api;

import java.util.List;

import org.example.aitoolruntime.core.model.ScanScope;
import org.example.aitoolruntime.core.model.ToolMethodDescriptor;

/**
 * 工具方法发现的核心 API。
 *
 * <p>根据给定的 {@link ScanScope} 发现符合条件的业务方法，并生成稳定的
 * {@link ToolMethodDescriptor} 列表。该接口不依赖 Spring / Spring AI，
 * 由上层（spring 模块）提供基于 Spring 容器实现。</p>
 */
public interface ToolMethodDiscovery {

    /**
     * 在指定扫描范围内发现候选业务方法。
     *
     * @param scope 扫描范围，不能为 {@code null}
     * @return 发现的工具方法描述（稳定、去重、保持确定性顺序）
     */
    List<ToolMethodDescriptor> discover(ScanScope scope);
}
