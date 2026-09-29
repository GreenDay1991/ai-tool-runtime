package org.example.aitoolruntime.springai.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Agent 调用链（不可变）。
 *
 * <p>按嵌套顺序记录已参与的 Agent 标识，用于防循环调用（套娃）与最大嵌套深度检测。
 * 调用链属于 Tool Runtime 的执行上下文，随 {@code ToolContext} 在嵌套调用间传递，
 * 不依赖 LLM 自行维护。</p>
 */
public final class AgentInvocationChain {

    private static final AgentInvocationChain EMPTY = new AgentInvocationChain(List.of());

    private final List<String> agents;

    private AgentInvocationChain(List<String> agents) {
        this.agents = List.copyOf(agents);
    }

    public static AgentInvocationChain empty() {
        return EMPTY;
    }

    public static AgentInvocationChain of(List<String> agents) {
        Objects.requireNonNull(agents, "agents");
        return agents.isEmpty() ? EMPTY : new AgentInvocationChain(agents);
    }

    /** 追加一个 Agent 到链尾，返回新链。 */
    public AgentInvocationChain append(String agentId) {
        Objects.requireNonNull(agentId, "agentId");
        List<String> next = new ArrayList<>(agents);
        next.add(agentId);
        return new AgentInvocationChain(next);
    }

    /** 当前 Agent 是否已经存在于链中（即循环调用）。 */
    public boolean contains(String agentId) {
        return agents.contains(agentId);
    }

    /** 当前链深度（已参与调用的 Agent 数量）。 */
    public int depth() {
        return agents.size();
    }

    /** 链中 Agent 标识（不可变、按嵌套顺序）。 */
    public List<String> agents() {
        return agents;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AgentInvocationChain other)) {
            return false;
        }
        return agents.equals(other.agents);
    }

    @Override
    public int hashCode() {
        return agents.hashCode();
    }

    @Override
    public String toString() {
        return String.join(" -> ", agents);
    }
}
