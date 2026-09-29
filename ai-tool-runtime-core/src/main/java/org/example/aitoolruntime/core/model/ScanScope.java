package org.example.aitoolruntime.core.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 描述一次工具方法发现扫描的范围。
 *
 * <p>扫描范围决定了哪些 Spring Bean 以及 Bean 上的哪些方法会被当作动态工具的候选。
 * 所有注解均以全限定名（字符串）表达，以保证 core 模块不依赖 Spring / Spring AI。</p>
 *
 * <p>默认行为（与项目约定一致）：</p>
 * <ul>
 *     <li>纳入：{@code @Service}、{@code @Repository} 标注的 Bean；</li>
 *     <li>排除：{@code @Controller}、{@code @RestController}、{@code @Configuration}、
 *         普通 {@code @Component}、基础设施 Bean、Util / 普通 Java 类；</li>
 *     <li>方法层面：已标记 Spring AI {@code @Tool} 的方法直接排除。</li>
 * </ul>
 */
public final class ScanScope {

    /** Spring AI {@code @Tool} 注解的全限定名。 */
    public static final String SPRING_AI_TOOL_ANNOTATION = "org.springframework.ai.tool.annotation.Tool";

    public static final String SERVICE_ANNOTATION = "org.springframework.stereotype.Service";
    public static final String REPOSITORY_ANNOTATION = "org.springframework.stereotype.Repository";
    public static final String CONTROLLER_ANNOTATION = "org.springframework.stereotype.Controller";
    public static final String REST_CONTROLLER_ANNOTATION = "org.springframework.web.bind.annotation.RestController";
    public static final String CONFIGURATION_ANNOTATION = "org.springframework.context.annotation.Configuration";
    public static final String COMPONENT_ANNOTATION = "org.springframework.stereotype.Component";

    private final Set<String> basePackages;
    private final Set<String> includeBeanAnnotations;
    private final Set<String> excludeBeanAnnotations;
    private final Set<String> excludedBeanNames;
    private final Set<String> excludedMethodAnnotations;
    private final Set<String> excludedMethodNames;

    private ScanScope(Builder builder) {
        this.basePackages = Collections.unmodifiableSet(new LinkedHashSet<>(builder.basePackages));
        this.includeBeanAnnotations = Collections.unmodifiableSet(new LinkedHashSet<>(builder.includeBeanAnnotations));
        this.excludeBeanAnnotations = Collections.unmodifiableSet(new LinkedHashSet<>(builder.excludeBeanAnnotations));
        this.excludedBeanNames = Collections.unmodifiableSet(new LinkedHashSet<>(builder.excludedBeanNames));
        this.excludedMethodAnnotations = Collections.unmodifiableSet(new LinkedHashSet<>(builder.excludedMethodAnnotations));
        this.excludedMethodNames = Collections.unmodifiableSet(new LinkedHashSet<>(builder.excludedMethodNames));
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ScanScope defaults() {
        return builder().build();
    }

    /** 基础包名（前缀匹配）；为空表示不按包过滤。 */
    public Set<String> getBasePackages() {
        return basePackages;
    }

    /** 纳入的 Bean 注解全限定名（默认 {@code @Service}、{@code @Repository}）。 */
    public Set<String> getIncludeBeanAnnotations() {
        return includeBeanAnnotations;
    }

    /** 显式排除的 Bean 注解全限定名（默认 Controller / RestController / Configuration / Component）。 */
    public Set<String> getExcludeBeanAnnotations() {
        return excludeBeanAnnotations;
    }

    /** 按 Bean 名排除。 */
    public Set<String> getExcludedBeanNames() {
        return excludedBeanNames;
    }

    /** 排除的方法注解全限定名（默认 Spring AI {@code @Tool}）。 */
    public Set<String> getExcludedMethodAnnotations() {
        return excludedMethodAnnotations;
    }

    /** 排除的方法名（默认 Object 方法 + Spring 生命周期方法）。 */
    public Set<String> getExcludedMethodNames() {
        return excludedMethodNames;
    }

    /** {@link ScanScope} 构建器。 */
    public static final class Builder {

        private final Set<String> basePackages = new LinkedHashSet<>();

        private final Set<String> includeBeanAnnotations = new LinkedHashSet<>();
        private final Set<String> excludeBeanAnnotations = new LinkedHashSet<>();
        private final Set<String> excludedBeanNames = new LinkedHashSet<>();
        private final Set<String> excludedMethodAnnotations = new LinkedHashSet<>();
        private final Set<String> excludedMethodNames = new LinkedHashSet<>();

        public Builder() {
            this.includeBeanAnnotations.add(SERVICE_ANNOTATION);
            this.includeBeanAnnotations.add(REPOSITORY_ANNOTATION);

            this.excludeBeanAnnotations.add(CONTROLLER_ANNOTATION);
            this.excludeBeanAnnotations.add(REST_CONTROLLER_ANNOTATION);
            this.excludeBeanAnnotations.add(CONFIGURATION_ANNOTATION);
            this.excludeBeanAnnotations.add(COMPONENT_ANNOTATION);

            this.excludedMethodAnnotations.add(SPRING_AI_TOOL_ANNOTATION);

            // Object 方法
            this.excludedMethodNames.add("equals");
            this.excludedMethodNames.add("hashCode");
            this.excludedMethodNames.add("toString");
            this.excludedMethodNames.add("clone");
            this.excludedMethodNames.add("finalize");
            this.excludedMethodNames.add("getClass");
            this.excludedMethodNames.add("notify");
            this.excludedMethodNames.add("notifyAll");
            this.excludedMethodNames.add("wait");
            // Spring 生命周期 / 基础设施方法
            this.excludedMethodNames.add("afterPropertiesSet");
            this.excludedMethodNames.add("destroy");
            this.excludedMethodNames.add("setBeanFactory");
            this.excludedMethodNames.add("setBeanName");
            this.excludedMethodNames.add("setApplicationContext");
            this.excludedMethodNames.add("setEnvironment");
            this.excludedMethodNames.add("setResourceLoader");
            this.excludedMethodNames.add("setMessageSource");
            this.excludedMethodNames.add("setServletContext");
            this.excludedMethodNames.add("afterSingletonsInstantiated");
            this.excludedMethodNames.add("start");
            this.excludedMethodNames.add("stop");
            this.excludedMethodNames.add("isRunning");
            this.excludedMethodNames.add("getPhase");
        }

        public Builder addBasePackage(String basePackage) {
            this.basePackages.add(basePackage);
            return this;
        }

        public Builder addIncludeBeanAnnotation(String annotationName) {
            this.includeBeanAnnotations.add(annotationName);
            return this;
        }

        public Builder addExcludeBeanAnnotation(String annotationName) {
            this.excludeBeanAnnotations.add(annotationName);
            return this;
        }

        public Builder addExcludedBeanName(String beanName) {
            this.excludedBeanNames.add(beanName);
            return this;
        }

        public Builder addExcludedMethodAnnotation(String annotationName) {
            this.excludedMethodAnnotations.add(annotationName);
            return this;
        }

        public Builder addExcludedMethodName(String methodName) {
            this.excludedMethodNames.add(methodName);
            return this;
        }

        public ScanScope build() {
            return new ScanScope(this);
        }
    }
}
