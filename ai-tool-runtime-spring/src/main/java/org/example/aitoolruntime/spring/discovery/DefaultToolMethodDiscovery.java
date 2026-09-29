package org.example.aitoolruntime.spring.discovery;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.example.aitoolruntime.core.api.ToolMethodDiscovery;
import org.example.aitoolruntime.core.model.ScanScope;
import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.example.aitoolruntime.core.model.ToolMethodId;
import org.example.aitoolruntime.core.model.ToolMethodParameter;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.lang.Nullable;

/**
 * {@link ToolMethodDiscovery} 的默认 Spring 实现。
 *
 * <p>调用链：{@code ScanScope → ApplicationContext → Bean 发现 → Method 发现 → ToolMethodDescriptor}。</p>
 */
public class DefaultToolMethodDiscovery implements ToolMethodDiscovery {

    private final SpringBeanScanner beanScanner;
    private final SpringMethodScanner methodScanner;
    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();

    public DefaultToolMethodDiscovery(SpringBeanScanner beanScanner, SpringMethodScanner methodScanner) {
        this.beanScanner = beanScanner;
        this.methodScanner = methodScanner;
    }

    @Override
    public List<ToolMethodDescriptor> discover(ScanScope scope) {
        List<ToolMethodDescriptor> descriptors = new ArrayList<>();
        for (ScannedSpringBean bean : beanScanner.scan(scope)) {
            for (Method method : methodScanner.scan(bean.type(), scope)) {
                descriptors.add(toDescriptor(bean, method));
            }
        }
        return descriptors;
    }

    private ToolMethodDescriptor toDescriptor(ScannedSpringBean bean, Method method) {
        ToolMethodId id = ToolMethodId.of(bean.name(), method.getName(), method.getParameterTypes());
        return ToolMethodDescriptor.builder()
                .id(id)
                .beanName(bean.name())
                .beanType(bean.type())
                .methodName(method.getName())
                .methodSignature(buildSignature(method))
                .parameters(buildParameters(method))
                .returnType(method.getReturnType())
                .build();
    }

    private List<ToolMethodParameter> buildParameters(Method method) {
        String[] names = parameterNameDiscoverer.getParameterNames(method);
        Parameter[] parameters = method.getParameters();
        List<ToolMethodParameter> result = new ArrayList<>(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            String name = (names != null && i < names.length) ? names[i] : ("arg" + i);
            boolean required = !parameters[i].isAnnotationPresent(Nullable.class);
            result.add(new ToolMethodParameter(name, parameters[i].getType(), required));
        }
        return result;
    }

    private String buildSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append(method.getReturnType().getName()).append(' ').append(method.getName()).append('(');
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(types[i].getName());
        }
        return sb.append(')').toString();
    }
}
