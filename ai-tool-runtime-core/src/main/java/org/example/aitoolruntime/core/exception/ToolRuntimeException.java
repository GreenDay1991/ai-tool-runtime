package org.example.aitoolruntime.core.exception;

/**
 * ai-tool-runtime 的基础运行时异常。
 */
public class ToolRuntimeException extends RuntimeException {

    public ToolRuntimeException(String message) {
        super(message);
    }

    public ToolRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
