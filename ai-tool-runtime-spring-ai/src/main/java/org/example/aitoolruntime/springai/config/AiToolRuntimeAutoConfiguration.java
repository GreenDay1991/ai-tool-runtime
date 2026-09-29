package org.example.aitoolruntime.springai.config;

import org.example.aitoolruntime.core.api.ToolMethodDiscovery;
import org.example.aitoolruntime.spring.config.AiToolRuntimeSpringConfiguration;
import org.example.aitoolruntime.spring.resolver.SpringBeanResolver;
import org.example.aitoolruntime.spring.resolver.SpringMethodResolver;
import org.example.aitoolruntime.springai.factory.MethodToolCallbackFactory;
import org.example.aitoolruntime.springai.factory.SecuredToolCallbackFactory;
import org.example.aitoolruntime.springai.runtime.AiToolRuntime;
import org.example.aitoolruntime.springai.runtime.AiToolRuntimeImpl;
import org.example.aitoolruntime.springai.security.DefaultToolAuthorizationManager;
import org.example.aitoolruntime.springai.security.RuntimeApiPolicy;
import org.example.aitoolruntime.springai.security.ToolAuthorization;
import org.example.aitoolruntime.springai.security.ToolAuthorizationManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * spring-ai 模块的自动装配。
 *
 * <p>默认提供：授权管理器（身份认证 + 循环/深度检测 + 授权）、内部工具工厂与
 * {@link AiToolRuntime}。宿主应用可通过自定义 {@link ToolAuthorization} /
 * {@link ToolAuthorizationManager} / {@link AiToolRuntime} Bean 覆盖默认行为。</p>
 */
@AutoConfiguration
@Import(AiToolRuntimeSpringConfiguration.class)
@EnableConfigurationProperties(AiToolRuntimeProperties.class)
public class AiToolRuntimeAutoConfiguration {

    /**
     * 默认授权策略：放行（仅保证身份存在 + 无循环 + 未超深度）。
     * 生产环境应由宿主应用提供具体权限实现覆盖本 Bean。
     */
    @Bean
    @ConditionalOnMissingBean(ToolAuthorization.class)
    public ToolAuthorization defaultToolAuthorization() {
        return toolContext -> true;
    }

    @Bean
    @ConditionalOnMissingBean(ToolAuthorizationManager.class)
    public ToolAuthorizationManager toolAuthorizationManager(AiToolRuntimeProperties properties,
            ToolAuthorization toolAuthorization) {
        return new DefaultToolAuthorizationManager(toolAuthorization, properties.getMaxDepth());
    }

    @Bean
    @ConditionalOnMissingBean(RuntimeApiPolicy.class)
    public RuntimeApiPolicy runtimeApiPolicy(AiToolRuntimeProperties properties) {
        return new RuntimeApiPolicy(properties.getRuntimeApi().isAllowToolExecution());
    }

    @Bean
    public MethodToolCallbackFactory methodToolCallbackFactory(SpringBeanResolver beanResolver,
            SpringMethodResolver methodResolver) {
        return new MethodToolCallbackFactory(beanResolver, methodResolver);
    }

    @Bean
    public SecuredToolCallbackFactory securedToolCallbackFactory(ToolAuthorizationManager authorizationManager,
            RuntimeApiPolicy runtimeApiPolicy) {
        return new SecuredToolCallbackFactory(authorizationManager, runtimeApiPolicy);
    }

    @Bean
    @ConditionalOnMissingBean(AiToolRuntime.class)
    public AiToolRuntime aiToolRuntime(ToolMethodDiscovery discovery,
            MethodToolCallbackFactory methodToolCallbackFactory,
            SecuredToolCallbackFactory securedToolCallbackFactory) {
        return new AiToolRuntimeImpl(discovery, methodToolCallbackFactory, securedToolCallbackFactory);
    }
}
