package org.example.aitoolruntime.spring.discovery;

/**
 * 一次 Bean 扫描的结果：Bean 名与其声明的类型。
 *
 * @param name Spring Bean 名
 * @param type Bean 类型（来自 {@code ApplicationContext#getType(String)}）
 */
public record ScannedSpringBean(String name, Class<?> type) {
}
