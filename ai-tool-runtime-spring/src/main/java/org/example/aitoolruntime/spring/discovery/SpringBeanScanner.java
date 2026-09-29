package org.example.aitoolruntime.spring.discovery;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.example.aitoolruntime.core.model.ScanScope;
import org.springframework.context.ApplicationContext;

/**
 * 基于 Spring {@link ApplicationContext} 的 Bean 扫描器。
 *
 * <p>遍历容器中全部 Bean 定义，解析其类型并通过 {@link SpringBeanFilter} 筛选，
 * 返回按 Bean 名排序的候选 Bean 列表。此处只做 Bean 级别的发现，不实例化 Bean。</p>
 */
public class SpringBeanScanner {

    private final ApplicationContext applicationContext;
    private final SpringBeanFilter beanFilter;

    public SpringBeanScanner(ApplicationContext applicationContext, SpringBeanFilter beanFilter) {
        this.applicationContext = applicationContext;
        this.beanFilter = beanFilter;
    }

    /**
     * 扫描并返回符合范围的候选 Bean。
     */
    public List<ScannedSpringBean> scan(ScanScope scope) {
        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames);

        List<ScannedSpringBean> result = new ArrayList<>();
        for (String beanName : beanNames) {
            Class<?> beanType = applicationContext.getType(beanName);
            if (beanType == null) {
                continue;
            }
            if (beanFilter.test(beanName, beanType, scope)) {
                result.add(new ScannedSpringBean(beanName, beanType));
            }
        }
        result.sort(Comparator.comparing(ScannedSpringBean::name));
        return result;
    }
}
