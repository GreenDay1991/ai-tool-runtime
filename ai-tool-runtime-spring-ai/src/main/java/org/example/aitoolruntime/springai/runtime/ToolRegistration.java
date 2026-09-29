package org.example.aitoolruntime.springai.runtime;

import java.util.Objects;

import org.example.aitoolruntime.core.model.ToolMethodId;

/**
 * 单次工具注册的入参。
 *
 * @param methodId    目标方法标识
 * @param name        工具名
 * @param description 工具描述
 * @param runtimeApi  是否为 Runtime API 工具
 */
public record ToolRegistration(ToolMethodId methodId, String name, String description, boolean runtimeApi) {

    public ToolRegistration {
        Objects.requireNonNull(methodId, "methodId");
        Objects.requireNonNull(name, "name");
        Objects.requireNonNull(description, "description");
    }

    /** 普通业务工具的注册入参。 */
    public static ToolRegistration of(ToolMethodId methodId, String name, String description) {
        return new ToolRegistration(methodId, name, description, false);
    }

    /** Runtime API 工具的注册入参。 */
    public static ToolRegistration runtimeApi(ToolMethodId methodId, String name, String description) {
        return new ToolRegistration(methodId, name, description, true);
    }
}
