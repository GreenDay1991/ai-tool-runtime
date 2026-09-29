package org.example.aitoolruntime.springai.factory;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.example.aitoolruntime.core.model.ToolMethodDescriptor;
import org.example.aitoolruntime.core.model.ToolMethodParameter;

/**
 * 从 {@link ToolMethodDescriptor} 生成简单的 JSON Schema 输入描述。
 *
 * <p>该 Schema 供 AI 模型理解工具入参；实际的 JSON→参数反序列化由 Spring AI 依据方法
 * 签名完成，因此本实现只需生成与参数一一对应的对象 Schema 即可。</p>
 */
public final class ToolInputSchemaGenerator {

    private ToolInputSchemaGenerator() {
    }

    public static String generate(ToolMethodDescriptor descriptor) {
        List<ToolMethodParameter> parameters = descriptor.getParameters();
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"object\",\"properties\":{");

        List<String> required = new ArrayList<>();
        for (int i = 0; i < parameters.size(); i++) {
            ToolMethodParameter parameter = parameters.get(i);
            if (i > 0) {
                sb.append(',');
            }
            sb.append('"').append(escape(parameter.name()))
                    .append("\":{\"type\":\"").append(jsonType(parameter.type())).append("\"}");
            if (parameter.required()) {
                required.add(parameter.name());
            }
        }
        sb.append('}');

        if (!required.isEmpty()) {
            sb.append(",\"required\":[");
            for (int i = 0; i < required.size(); i++) {
                if (i > 0) {
                    sb.append(',');
                }
                sb.append('"').append(escape(required.get(i))).append('"');
            }
            sb.append(']');
        }
        return sb.append('}').toString();
    }

    private static String jsonType(Class<?> type) {
        if (type == String.class || type == char.class || type == Character.class || type.isEnum()) {
            return "string";
        }
        if (type == boolean.class || type == Boolean.class) {
            return "boolean";
        }
        if (type == int.class || type == Integer.class || type == long.class || type == Long.class
                || type == short.class || type == Short.class || type == byte.class || type == Byte.class
                || type == BigInteger.class) {
            return "integer";
        }
        if (type == double.class || type == Double.class || type == float.class || type == Float.class
                || type == BigDecimal.class || type == Number.class) {
            return "number";
        }
        if (type.isArray() || Collection.class.isAssignableFrom(type)) {
            return "array";
        }
        if (Map.class.isAssignableFrom(type)) {
            return "object";
        }
        return "object";
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
