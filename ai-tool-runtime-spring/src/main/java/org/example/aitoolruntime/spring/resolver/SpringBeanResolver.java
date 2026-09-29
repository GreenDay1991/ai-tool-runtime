package org.example.aitoolruntime.spring.resolver;

import org.springframework.context.ApplicationContext;

/**
 * 通过 Spring {@link ApplicationContext} 获取真实 Spring Bean。
 *
 * <p>禁止在创建工具时 {@code new Service()}，必须经由本解析器取得容器中的真实 Bean / 代理对象，
 * 从而保留 Spring AOP、{@code @Transactional}、Spring Security 等代理能力。</p>
 */
public class SpringBeanResolver {

    private final ApplicationContext applicationContext;

    public SpringBeanResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * 按 Bean 名解析真实 Bean（可能是代理对象）。
     */
    public Object resolve(String beanName) {
        return applicationContext.getBean(beanName);
    }

    /**
     * 按类型解析真实 Bean（要求类型唯一）。
     */
    public Object resolve(Class<?> beanType) {
        return applicationContext.getBean(beanType);
    }
}
