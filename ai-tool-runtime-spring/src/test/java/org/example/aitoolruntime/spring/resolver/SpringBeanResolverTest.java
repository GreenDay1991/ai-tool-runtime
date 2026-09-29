package org.example.aitoolruntime.spring.resolver;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Method;
import java.util.List;

import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.example.aitoolruntime.core.model.ToolMethodId;
import org.example.aitoolruntime.core.model.ToolMethodParameter;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

class SpringBeanResolverTest {

    @Test
    void resolvesRealSpringBeanAndInvokesThroughProxy() throws Exception {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            SpringBeanResolver beanResolver = new SpringBeanResolver(context);
            SpringMethodResolver methodResolver = new SpringMethodResolver();

            Object bean = beanResolver.resolve("greetingService");

            // 容器返回的是 JDK 动态代理，而非原始实现实例
            assertThat(AopUtils.isJdkDynamicProxy(bean)).isTrue();
            assertThat(bean).isSameAs(context.getBean("greetingService"));

            // 方法解析穿透代理，落到目标类上
            ToolMethodDescriptor descriptor = ToolMethodDescriptor.builder()
                    .id(ToolMethodId.of("greetingService", "greet", new Class<?>[] { String.class }))
                    .beanName("greetingService")
                    .beanType(GreetingService.class)
                    .methodName("greet")
                    .methodSignature("java.lang.String greet(java.lang.String)")
                    .parameters(List.of(ToolMethodParameter.of("name", String.class)))
                    .returnType(String.class)
                    .build();

            Method method = methodResolver.resolve(descriptor, bean);
            Object result = method.invoke(bean, "world");

            assertThat(result).isEqualTo("Hello, world");
        }
    }

    interface GreetingService {
        String greet(String name);
    }

    @Service
    static class GreetingServiceImpl implements GreetingService {
        @Override
        public String greet(String name) {
            return "Hello, " + name;
        }
    }

    @Configuration
    static class TestConfig {

        @Bean
        GreetingService greetingService() {
            GreetingServiceImpl target = new GreetingServiceImpl();
            ProxyFactory factory = new ProxyFactory(target);
            factory.setProxyTargetClass(false);
            return (GreetingService) factory.getProxy();
        }
    }
}
