package org.example.aitoolruntime.core.exception;

import org.example.aitoolruntime.core.model.ToolMethodId;

/**
 * 当无法找到指定 {@link ToolMethodId} 对应的方法时抛出。
 */
public class ToolMethodNotFoundException extends ToolRuntimeException {

    public ToolMethodNotFoundException(ToolMethodId id) {
        super("Tool method not found: " + id);
    }

    public ToolMethodNotFoundException(String message) {
        super(message);
    }
}
