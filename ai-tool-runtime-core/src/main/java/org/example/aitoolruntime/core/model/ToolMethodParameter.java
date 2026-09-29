package org.example.aitoolruntime.core.model;

import java.util.Objects;

/**
 * 工具方法的单个参数描述。
 *
 * @param name     参数名（依赖编译参数 {@code -parameters} 或参数名发现器）
 * @param type     参数类型
 * @param required 是否为必填参数
 */
public record ToolMethodParameter(String name, Class<?> type, boolean required) {

    public ToolMethodParameter {
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(type, "type");
    }

    public static ToolMethodParameter of(String name, Class<?> type) {
        return new ToolMethodParameter(name, type, true);
    }
}
