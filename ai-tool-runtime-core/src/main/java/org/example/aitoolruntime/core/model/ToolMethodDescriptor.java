package org.example.aitoolruntime.core.model;

import java.util.List;
import java.util.Objects;

/**
 * 已发现业务方法的完整描述，是 Runtime 创建动态工具的依据。
 *
 * <p>描述对象是不可变的，其唯一性由 {@link #getId()} 保证。</p>
 */
public final class ToolMethodDescriptor {

    private final ToolMethodId id;
    private final String beanName;
    private final Class<?> beanType;
    private final String methodName;
    private final String methodSignature;
    private final List<ToolMethodParameter> parameters;
    private final Class<?> returnType;

    private ToolMethodDescriptor(Builder builder) {
        this.id = Objects.requireNonNull(builder.id, "id");
        this.beanName = Objects.requireNonNull(builder.beanName, "beanName");
        this.beanType = Objects.requireNonNull(builder.beanType, "beanType");
        this.methodName = Objects.requireNonNull(builder.methodName, "methodName");
        this.methodSignature = Objects.requireNonNull(builder.methodSignature, "methodSignature");
        this.parameters = List.copyOf(Objects.requireNonNull(builder.parameters, "parameters"));
        this.returnType = Objects.requireNonNull(builder.returnType, "returnType");
    }

    public static Builder builder() {
        return new Builder();
    }

    public ToolMethodId getId() {
        return id;
    }

    public String getBeanName() {
        return beanName;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

    public String getMethodName() {
        return methodName;
    }

    /** 人类可读的方法签名，例如 {@code java.lang.String findById(java.lang.Long)}。 */
    public String getMethodSignature() {
        return methodSignature;
    }

    public List<ToolMethodParameter> getParameters() {
        return parameters;
    }

    public Class<?> getReturnType() {
        return returnType;
    }

    /** {@link ToolMethodDescriptor} 构建器。 */
    public static final class Builder {

        private ToolMethodId id;
        private String beanName;
        private Class<?> beanType;
        private String methodName;
        private String methodSignature;
        private List<ToolMethodParameter> parameters = List.of();
        private Class<?> returnType;

        public Builder id(ToolMethodId id) {
            this.id = id;
            return this;
        }

        public Builder beanName(String beanName) {
            this.beanName = beanName;
            return this;
        }

        public Builder beanType(Class<?> beanType) {
            this.beanType = beanType;
            return this;
        }

        public Builder methodName(String methodName) {
            this.methodName = methodName;
            return this;
        }

        public Builder methodSignature(String methodSignature) {
            this.methodSignature = methodSignature;
            return this;
        }

        public Builder parameters(List<ToolMethodParameter> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder returnType(Class<?> returnType) {
            this.returnType = returnType;
            return this;
        }

        public ToolMethodDescriptor build() {
            return new ToolMethodDescriptor(this);
        }
    }
}
