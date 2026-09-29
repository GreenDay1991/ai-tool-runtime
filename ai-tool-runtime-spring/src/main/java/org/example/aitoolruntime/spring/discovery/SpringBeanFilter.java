package org.example.aitoolruntime.spring.discovery;

import java.util.Set;

import org.example.aitoolruntime.core.model.ScanScope;
import org.springframework.core.annotation.MergedAnnotations;
import org.springframework.core.annotation.MergedAnnotations.SearchStrategy;

/**
 * Bean 类型筛选器。
 *
 * <p>筛选顺序：</p>
 * <ol>
 *     <li>按 Bean 名排除；</li>
 *     <li>按基础包过滤（前缀匹配）；</li>
 *     <li>命中 {@link ScanScope#getIncludeBeanAnnotations()} 则纳入
 *         （默认 {@code @Service}、{@code @Repository}，含元注解解析）；</li>
 *     <li>命中 {@link ScanScope#getExcludeBeanAnnotations()} 则排除
 *         （默认 Controller / RestController / Configuration / Component）；</li>
 *     <li>其余（基础设施 Bean、Util / 普通 Java 类）一律排除。</li>
 * </ol>
 */
public class SpringBeanFilter {

    /**
     * 判断指定 Bean 是否应被纳入动态工具发现。
     */
    public boolean test(String beanName, Class<?> beanType, ScanScope scope) {
        if (scope.getExcludedBeanNames().contains(beanName)) {
            return false;
        }
        if (!matchesBasePackage(beanType, scope)) {
            return false;
        }
        if (hasAnyAnnotation(beanType, scope.getIncludeBeanAnnotations())) {
            return true;
        }
        if (hasAnyAnnotation(beanType, scope.getExcludeBeanAnnotations())) {
            return false;
        }
        return false;
    }

    private boolean matchesBasePackage(Class<?> beanType, ScanScope scope) {
        if (scope.getBasePackages().isEmpty()) {
            return true;
        }
        String packageName = beanType.getPackageName();
        for (String basePackage : scope.getBasePackages()) {
            if (packageName.equals(basePackage) || packageName.startsWith(basePackage + ".")) {
                return true;
            }
        }
        return false;
    }

    private boolean hasAnyAnnotation(Class<?> type, Set<String> annotationNames) {
        if (annotationNames.isEmpty()) {
            return false;
        }
        return MergedAnnotations.from(type, SearchStrategy.TYPE_HIERARCHY)
                .stream()
                .map(annotation -> annotation.getType().getName())
                .anyMatch(annotationNames::contains);
    }
}
