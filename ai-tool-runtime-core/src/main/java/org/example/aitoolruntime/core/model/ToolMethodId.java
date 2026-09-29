package org.example.aitoolruntime.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 工具方法的稳定唯一标识。
 *
 * <p>标识由 {@code beanName}、{@code methodName} 与参数类型全限定名列表共同构成，
 * 从而能够唯一区分：不同 Bean、不同方法以及同名方法的重载（overload）。</p>
 *
 * <p>标识不依赖任何运行时随机值，只由可反射获得的结构化信息计算而来，因此是稳定的。</p>
 *
 * @param beanName       Spring Bean 名
 * @param methodName     方法名
 * @param parameterTypes 参数类型全限定名（保持声明顺序，用于区分重载）
 */
public record ToolMethodId(String beanName, String methodName, List<String> parameterTypes) {

    public ToolMethodId {
        Objects.requireNonNull(beanName, "beanName");
        Objects.requireNonNull(methodName, "methodName");
        parameterTypes = List.copyOf(Objects.requireNonNull(parameterTypes, "parameterTypes"));
    }

    public static ToolMethodId of(String beanName, String methodName, Class<?>[] parameterTypes) {
        List<String> types = Arrays.stream(parameterTypes).map(Class::getName).toList();
        return new ToolMethodId(beanName, methodName, types);
    }

    public static ToolMethodId of(String beanName, String methodName, List<String> parameterTypes) {
        return new ToolMethodId(beanName, methodName, parameterTypes);
    }

    /** 规范字符串形式：{@code beanName#methodName(paramType1,paramType2)}。 */
    @Override
    public String toString() {
        return beanName + "#" + methodName + "(" + String.join(",", parameterTypes) + ")";
    }

    /** 从 {@link #toString()} 的字符串形式反向解析。 */
    public static ToolMethodId from(String value) {
        Objects.requireNonNull(value, "value");
        int hash = value.indexOf('#');
        if (hash <= 0) {
            throw new IllegalArgumentException("Invalid ToolMethodId: " + value);
        }
        String beanName = value.substring(0, hash);
        int paren = value.indexOf('(', hash);
        if (paren < 0 || !value.endsWith(")")) {
            throw new IllegalArgumentException("Invalid ToolMethodId: " + value);
        }
        String methodName = value.substring(hash + 1, paren);
        String params = value.substring(paren + 1, value.length() - 1);
        List<String> types;
        if (params.isEmpty()) {
            types = List.of();
        }
        else {
            types = new ArrayList<>(Arrays.asList(params.split(",")));
        }
        return new ToolMethodId(beanName, methodName, types);
    }
}
