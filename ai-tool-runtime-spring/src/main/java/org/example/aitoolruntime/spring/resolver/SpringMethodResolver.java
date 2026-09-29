package org.example.aitoolruntime.spring.resolver;

import java.lang.reflect.Method;

import org.example.aitoolruntime.core.exception.ToolMethodNotFoundException;
import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.example.aitoolruntime.core.model.ToolMethodParameter;
import org.springframework.aop.support.AopUtils;
import org.springframework.util.ReflectionUtils;

/**
 * 从 {@link ToolMethodDescriptor} 解析出真实 {@link Method}。
 *
 * <p>解析基于 Bean 的真实目标类型（穿透代理），并根据参数类型精确匹配重载方法。</p>
 */
public class SpringMethodResolver {

    /**
     * 根据描述符解析真实 Bean 上的方法。
     *
     * @param descriptor 方法描述符
     * @param bean       已解析的真实 Bean（可能是代理对象）
     */
    public Method resolve(ToolMethodDescriptor descriptor, Object bean) {
        Class<?> targetClass = AopUtils.getTargetClass(bean);
        Class<?>[] parameterTypes = descriptor.getParameters().stream()
                .map(ToolMethodParameter::type)
                .toArray(Class<?>[]::new);
        Method method = ReflectionUtils.findMethod(targetClass, descriptor.getMethodName(), parameterTypes);
        if (method == null) {
            throw new ToolMethodNotFoundException(descriptor.getId());
        }
        return method;
    }
}
