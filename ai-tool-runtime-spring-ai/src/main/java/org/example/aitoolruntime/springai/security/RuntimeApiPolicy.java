package org.example.aitoolruntime.springai.security;

/**
 * Runtime API 是否允许被 Tool 化的策略。
 *
 * <p>默认 Runtime API（discover / register / registerBatch）只是普通 Java API，
 * 不允许在 Tool 执行上下文中被调用；仅当配置
 * {@code ai.tool-runtime.runtime-api.allow-tool-execution=true} 时，开发者方可主动
 * 将 Runtime API 注册为 Tool（且仍走完全相同的安全包装）。</p>
 */
public final class RuntimeApiPolicy {

    private final boolean allowToolExecution;

    public RuntimeApiPolicy(boolean allowToolExecution) {
        this.allowToolExecution = allowToolExecution;
    }

    public boolean isAllowToolExecution() {
        return allowToolExecution;
    }

    /**
     * 若当前工具是 Runtime API 工具且未开启执行，则拒绝。
     *
     * @param runtimeApiTool 当前工具是否为 Runtime API 工具
     */
    public void check(boolean runtimeApiTool) {
        if (runtimeApiTool && !allowToolExecution) {
            throw new ToolSecurityException(
                    "Runtime API is not allowed to execute as a tool "
                            + "(ai.tool-runtime.runtime-api.allow-tool-execution=false)");
        }
    }
}
