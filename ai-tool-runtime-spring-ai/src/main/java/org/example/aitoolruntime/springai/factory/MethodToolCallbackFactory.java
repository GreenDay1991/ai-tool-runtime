package org.example.aitoolruntime.springai.factory;

import java.lang.reflect.Method;
import java.util.Objects;

import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.example.aitoolruntime.spring.resolver.SpringBeanResolver;
import org.example.aitoolruntime.spring.resolver.SpringMethodResolver;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.definition.ToolDefinition;
import org.springframework.ai.tool.method.MethodToolCallback;

/**
 * 创建 Spring AI 原生 {@link MethodToolCallback} 的内部工厂。
 *
 * <p>该工厂生成的只是「无安全包装」的原始回调，属于内部能力；
 * Runtime 对外仅返回经 {@code SecuredToolCallback} 包装后的 Tool。</p>
 */
public class MethodToolCallbackFactory {

    private final SpringBeanResolver beanResolver;
    private final SpringMethodResolver methodResolver;

    public MethodToolCallbackFactory(SpringBeanResolver beanResolver, SpringMethodResolver methodResolver) {
        this.beanResolver = Objects.requireNonNull(beanResolver, "beanResolver");
        this.methodResolver = Objects.requireNonNull(methodResolver, "methodResolver");
    }

    /**
     * 依据描述符创建原始 {@link MethodToolCallback}（返回类型为 {@link ToolCallback}，不暴露具体实现）。
     *
     * @param descriptor  方法描述符
     * @param name        工具名（供模型识别）
     * @param description 工具描述（供模型理解何时调用）
     */
    public ToolCallback create(ToolMethodDescriptor descriptor, String name, String description) {
        Object bean = beanResolver.resolve(descriptor.getBeanName());
        Method method = methodResolver.resolve(descriptor, bean);

        ToolDefinition toolDefinition = ToolDefinition.builder()
                .name(name)
                .description(description)
                .inputSchema(ToolInputSchemaGenerator.generate(descriptor))
                .build();

        return MethodToolCallback.builder()
                .toolDefinition(toolDefinition)
                .toolMethod(method)
                .toolObject(bean)
                .build();
    }
}
