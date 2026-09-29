package org.example.aitoolruntime.springai.security;

/**
 * 工具安全校验失败（认证 / 授权 / 调用链 / 最大深度）时抛出的异常。
 */
public class ToolSecurityException extends RuntimeException {

    public ToolSecurityException(String message) {
        super(message);
    }
}
