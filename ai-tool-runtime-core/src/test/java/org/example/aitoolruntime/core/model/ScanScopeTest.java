package org.example.aitoolruntime.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ScanScopeTest {

    @Test
    void defaultsIncludeServiceAndRepository() {
        ScanScope scope = ScanScope.defaults();
        assertThat(scope.getIncludeBeanAnnotations())
                .contains(ScanScope.SERVICE_ANNOTATION, ScanScope.REPOSITORY_ANNOTATION);
    }

    @Test
    void defaultsExcludeControllerAndComponentAndConfiguration() {
        ScanScope scope = ScanScope.defaults();
        assertThat(scope.getExcludeBeanAnnotations())
                .contains(ScanScope.CONTROLLER_ANNOTATION,
                        ScanScope.REST_CONTROLLER_ANNOTATION,
                        ScanScope.CONFIGURATION_ANNOTATION,
                        ScanScope.COMPONENT_ANNOTATION);
    }

    @Test
    void defaultsExcludeSpringAiToolAnnotation() {
        ScanScope scope = ScanScope.defaults();
        assertThat(scope.getExcludedMethodAnnotations()).contains(ScanScope.SPRING_AI_TOOL_ANNOTATION);
    }

    @Test
    void builderIsCustomizable() {
        ScanScope scope = ScanScope.builder()
                .addBasePackage("com.example")
                .addExcludedBeanName("internalBean")
                .build();
        assertThat(scope.getBasePackages()).contains("com.example");
        assertThat(scope.getExcludedBeanNames()).contains("internalBean");
    }
}
