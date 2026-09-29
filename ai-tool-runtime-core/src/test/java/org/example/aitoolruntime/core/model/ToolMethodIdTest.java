package org.example.aitoolruntime.core.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Test;

class ToolMethodIdTest {

    @Test
    void distinguishesDifferentBeans() {
        ToolMethodId a = ToolMethodId.of("beanA", "find", new Class<?>[] { String.class });
        ToolMethodId b = ToolMethodId.of("beanB", "find", new Class<?>[] { String.class });
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void distinguishesDifferentMethods() {
        ToolMethodId a = ToolMethodId.of("beanA", "find", new Class<?>[] { String.class });
        ToolMethodId b = ToolMethodId.of("beanA", "delete", new Class<?>[] { String.class });
        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void distinguishesOverloads() throws Exception {
        Method find1 = Overloaded.class.getMethod("find", String.class);
        Method find2 = Overloaded.class.getMethod("find", String.class, int.class);

        ToolMethodId id1 = ToolMethodId.of("overloaded", "find", find1.getParameterTypes());
        ToolMethodId id2 = ToolMethodId.of("overloaded", "find", find2.getParameterTypes());

        assertThat(id1).isNotEqualTo(id2);
        assertThat(id1.parameterTypes()).containsExactly("java.lang.String");
        assertThat(id2.parameterTypes()).containsExactly("java.lang.String", "int");
    }

    @Test
    void isStableAcrossConstruction() {
        ToolMethodId a = ToolMethodId.of("bean", "find", new Class<?>[] { String.class, int.class });
        ToolMethodId b = ToolMethodId.of("bean", "find", List.of("java.lang.String", "int"));
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a.toString()).isEqualTo("bean#find(java.lang.String,int)");
    }

    @Test
    void roundTripsToString() {
        ToolMethodId original = ToolMethodId.of("bean", "find", new Class<?>[] { String.class, int.class });
        ToolMethodId parsed = ToolMethodId.from(original.toString());
        assertThat(parsed).isEqualTo(original);
    }

    @Test
    void parsesNoArgMethod() {
        ToolMethodId parsed = ToolMethodId.from("bean#ping()");
        assertThat(parsed.beanName()).isEqualTo("bean");
        assertThat(parsed.methodName()).isEqualTo("ping");
        assertThat(parsed.parameterTypes()).isEmpty();
    }

    @Test
    void rejectsMalformedValue() {
        assertThatThrownBy(() -> ToolMethodId.from("not-an-id"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static class Overloaded {
        public String find(String name) {
            return name;
        }

        public String find(String name, int limit) {
            return name;
        }
    }
}
