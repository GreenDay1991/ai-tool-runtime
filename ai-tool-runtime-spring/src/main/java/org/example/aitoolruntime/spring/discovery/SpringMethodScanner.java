package org.example.aitoolruntime.spring.discovery;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.example.aitoolruntime.core.model.ScanScope;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;
import org.springframework.util.ReflectionUtils;

/**
 * Bean 方法扫描器。
 *
 * <p>在 Bean 类型上发现「公开业务方法」作为动态工具候选，并排除：</p>
 * <ul>
 *     <li>非 public / 静态 / 抽象 / 桥接 / 合成方法；</li>
 *     <li>{@code Object} 方法与 Spring 生命周期方法（按名排除）；</li>
 *     <li>已标记 Spring AI {@code @Tool} 的方法（按全限定名排除，避免与原生 Tool 重复）。</li>
 * </ul>
 */
public class SpringMethodScanner {

    /**
     * 扫描指定 Bean 类型上的候选业务方法，返回确定性排序结果。
     */
    public List<Method> scan(Class<?> beanType, ScanScope scope) {
        List<Method> result = new ArrayList<>();
        for (Method method : ReflectionUtils.getUniqueDeclaredMethods(beanType)) {
            if (isCandidate(method, scope)) {
                result.add(method);
            }
        }
        result.sort(Comparator.comparing(this::methodKey));
        return result;
    }

    private boolean isCandidate(Method method, ScanScope scope) {
        int modifiers = method.getModifiers();
        if (!Modifier.isPublic(modifiers) || Modifier.isStatic(modifiers) || Modifier.isAbstract(modifiers)) {
            return false;
        }
        if (method.isSynthetic() || method.isBridge()) {
            return false;
        }
        if (scope.getExcludedMethodNames().contains(method.getName())) {
            return false;
        }
        return !hasExcludedAnnotation(method, scope.getExcludedMethodAnnotations());
    }

    private boolean hasExcludedAnnotation(Method method, Set<String> annotationNames) {
        if (annotationNames.isEmpty()) {
            return false;
        }
        return MergedAnnotations.from(method, SearchStrategy.TYPE_HIERARCHY)
                .stream()
                .map(annotation -> annotation.getType().getName())
                .anyMatch(annotationNames::contains);
    }

    private String methodKey(Method method) {
        return method.getName() + "(" + parameterTypeNames(method) + ")";
    }

    private String parameterTypeNames(Method method) {
        StringBuilder sb = new StringBuilder();
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(types[i].getName());
        }
        return sb.toString();
    }
}
