package org.example.aitoolruntime.springai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Runtime 配置属性，前缀 {@code ai.tool-runtime}。
 */
@ConfigurationProperties(prefix = "ai.tool-runtime")
public class AiToolRuntimeProperties {

    /** Agent 调用链最大嵌套深度，超过即拒绝。 */
    private int maxDepth = 10;

    private final RuntimeApi runtimeApi = new RuntimeApi();

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public RuntimeApi getRuntimeApi() {
        return runtimeApi;
    }

    public static class RuntimeApi {

        /** 是否允许将 Runtime API 注册为 Tool 并在 Tool 执行上下文中调用。默认 false。 */
        private boolean allowToolExecution = false;

        public boolean isAllowToolExecution() {
            return allowToolExecution;
        }

        public void setAllowToolExecution(boolean allowToolExecution) {
            this.allowToolExecution = allowToolExecution;
        }
    }
}
