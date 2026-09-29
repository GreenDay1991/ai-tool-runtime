package org.example.aitoolruntime.springai.runtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;

import org.example.aitoolruntime.core.api.ToolMethodDiscovery;
import org.example.aitoolruntime.core.exception.ToolMethodNotFoundException;
import org.example.aitoolruntime.core.model.ScanScope;
import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.example.aitoolruntime.core.model.ToolMethodId;
import org.example.aitoolruntime.springai.callback.SecuredToolCallback;
import org.example.aitoolruntime.springai.config.AiToolRuntimeAutoConfiguration;
import org.example.aitoolruntime.springai.security.ToolContextKeys;
import org.example.aitoolruntime.springai.security.ToolSecurityException;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.model.ToolContext;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

class AiToolRuntimeIntegrationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AiToolRuntimeAutoConfiguration.class, TestServiceConfig.class);

    @Test
    void autoConfigurationWiresRuntime() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(AiToolRuntime.class);
            assertThat(context).hasSingleBean(ToolMethodDiscovery.class);
        });
    }

    @Test
    void discoverAndCreateSecuredToolThatInvokesRealBean() {
        runner.run(applicationContext -> {
            AiToolRuntime runtime = applicationContext.getBean(AiToolRuntime.class);

            List<ToolMethodDescriptor> descriptors = runtime.discover(ScanScope.defaults());
            ToolMethodDescriptor ping = descriptors.stream()
                    .filter(d -> d.getMethodName().equals("ping"))
                    .findFirst().orElseThrow();

            ToolCallback tool = runtime.createTool(ping.getId(), "ping", "Ping the service");

            // Runtime 对外只返回安全包装后的 Tool
            assertThat(tool).isInstanceOf(SecuredToolCallback.class);

            // 携带可信身份即可执行，命中真实 Spring Bean 方法
            ToolContext toolContext = new ToolContext(Map.<String, Object>of(ToolContextKeys.AGENT_ID, "agent-a"));
            assertThat(tool.call("{}", toolContext)).contains("pong");

            // 无身份必须被拒绝（目标方法不会执行）
            assertThatThrownBy(() -> tool.call("{}", (ToolContext) null))
                    .isInstanceOf(ToolSecurityException.class);
        });
    }

    @Test
    void createToolWithUnknownMethodThrows() {
        runner.run(applicationContext -> {
            AiToolRuntime runtime = applicationContext.getBean(AiToolRuntime.class);
            runtime.discover(ScanScope.defaults());
            ToolMethodId unknown = ToolMethodId.of("unknown", "nope", new Class<?>[] {});
            assertThatThrownBy(() -> runtime.createTool(unknown, "x", "y"))
                    .isInstanceOf(ToolMethodNotFoundException.class);
        });
    }

    @Service
    static class PingService {

        public String ping() {
            return "pong";
        }
    }

    @Configuration
    static class TestServiceConfig {

        @Bean
        PingService pingService() {
            return new PingService();
        }
    }
}
