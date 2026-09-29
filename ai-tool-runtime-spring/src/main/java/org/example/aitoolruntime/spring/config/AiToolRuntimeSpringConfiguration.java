package org.example.aitoolruntime.spring.config;

import org.example.aitoolruntime.core.api.ToolMethodDiscovery;
import org.example.aitoolruntime.spring.discovery.DefaultToolMethodDiscovery;
import org.example.aitoolruntime.spring.discovery.SpringBeanFilter;
import org.example.aitoolruntime.spring.discovery.SpringBeanScanner;
import org.example.aitoolruntime.spring.discovery.SpringMethodScanner;
import org.example.aitoolruntime.spring.resolver.SpringBeanResolver;
import org.example.aitoolruntime.spring.resolver.SpringMethodResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * spring 模块的 Bean 装配。提供 Bean / Method 发现与解析能力。
 *
 * <p>该配置不依赖 Spring AI，可独立使用；spring-ai 模块的自动装配会导入本配置。</p>
 */
@Configuration
public class AiToolRuntimeSpringConfiguration {

    @Bean
    public SpringBeanFilter springBeanFilter() {
        return new SpringBeanFilter();
    }

    @Bean
    public SpringBeanScanner springBeanScanner(ApplicationContext applicationContext, SpringBeanFilter springBeanFilter) {
        return new SpringBeanScanner(applicationContext, springBeanFilter);
    }

    @Bean
    public SpringMethodScanner springMethodScanner() {
        return new SpringMethodScanner();
    }

    @Bean
    public ToolMethodDiscovery toolMethodDiscovery(SpringBeanScanner springBeanScanner,
            SpringMethodScanner springMethodScanner) {
        return new DefaultToolMethodDiscovery(springBeanScanner, springMethodScanner);
    }

    @Bean
    public SpringBeanResolver springBeanResolver(ApplicationContext applicationContext) {
        return new SpringBeanResolver(applicationContext);
    }

    @Bean
    public SpringMethodResolver springMethodResolver() {
        return new SpringMethodResolver();
    }
}
