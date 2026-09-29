package org.example.aitoolruntime.spring.discovery;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.example.aitoolruntime.core.model.ScanScope;
import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.junit.jupiter.api.Test;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

class DefaultToolMethodDiscoveryTest {

    @Test
    void discoversOnlyBusinessBeansAndExcludesToolMethods() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            DefaultToolMethodDiscovery discovery = new DefaultToolMethodDiscovery(
                    new SpringBeanScanner(context, new SpringBeanFilter()),
                    new SpringMethodScanner());

            List<ToolMethodDescriptor> descriptors = discovery.discover(ScanScope.defaults());

            Map<String, Set<String>> methodsByBean = descriptors.stream().collect(Collectors.groupingBy(
                    ToolMethodDescriptor::getBeanName,
                    Collectors.mapping(ToolMethodDescriptor::getMethodName, Collectors.toSet())));

            // @Service 纳入，@Tool 方法排除
            assertThat(methodsByBean.get("orderService")).containsExactlyInAnyOrder("createOrder", "findOrder");
            assertThat(methodsByBean.get("orderService")).doesNotContain("nativeTool");

            // @Repository 纳入
            assertThat(methodsByBean.get("orderRepository")).contains("save");

            // @Controller / 普通 @Component / @Configuration 排除
            assertThat(methodsByBean).doesNotContainKeys("orderController", "plainComponent");
            assertThat(methodsByBean.keySet()).noneMatch(name -> name.contains("TestConfig"));
        }
    }

    @Test
    void descriptorCarriesStableIdAndSignature() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            DefaultToolMethodDiscovery discovery = new DefaultToolMethodDiscovery(
                    new SpringBeanScanner(context, new SpringBeanFilter()),
                    new SpringMethodScanner());

            List<ToolMethodDescriptor> descriptors = discovery.discover(ScanScope.defaults());

            ToolMethodDescriptor createOrder = descriptors.stream()
                    .filter(d -> d.getMethodName().equals("createOrder"))
                    .findFirst().orElseThrow();

            assertThat(createOrder.getBeanName()).isEqualTo("orderService");
            assertThat(createOrder.getBeanType()).isEqualTo(OrderService.class);
            assertThat(createOrder.getReturnType()).isEqualTo(String.class);
            assertThat(createOrder.getParameters()).hasSize(1);
            assertThat(createOrder.getParameters().get(0).name()).isEqualTo("item");
            assertThat(createOrder.getId().toString()).isEqualTo("orderService#createOrder(java.lang.String)");
        }
    }

    @Configuration
    static class TestConfig {

        @Bean
        OrderService orderService() {
            return new OrderService();
        }

        @Bean
        OrderRepository orderRepository() {
            return new OrderRepository();
        }

        @Bean
        OrderController orderController() {
            return new OrderController();
        }

        @Bean
        PlainComponent plainComponent() {
            return new PlainComponent();
        }
    }

    @Service
    static class OrderService {

        public String createOrder(String item) {
            return "order:" + item;
        }

        public String findOrder(String item, int limit) {
            return "find:" + item;
        }

        @Tool(description = "原生 Spring AI Tool，必须被排除")
        public String nativeTool() {
            return "native";
        }
    }

    @Repository
    static class OrderRepository {

        public String save(String data) {
            return "saved:" + data;
        }
    }

    @Controller
    static class OrderController {

        public String handle() {
            return "controller";
        }
    }

    @Component
    static class PlainComponent {

        public String doStuff() {
            return "component";
        }
    }
}
