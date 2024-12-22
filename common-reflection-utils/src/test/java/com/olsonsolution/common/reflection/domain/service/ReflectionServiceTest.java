package com.olsonsolution.common.reflection.domain.service;

import com.olsonsolution.common.reflection.domain.repository.ReflectionUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ReflectionServiceTest {

    private final ReflectionUtils reflectionUtils = new com.olsonsolution.common.reflection.domain.service.ReflectionUtils();

    @Test
    void shouldTrueOnSameAssignableParamTypes() {
        Field field1 = reflectionUtils.findField(AssignableList.class, "charSequences", false)
                .orElseThrow();
        Field field2 = reflectionUtils.findField(AssignableList.class, "strings", false)
                .orElseThrow();
        assertThat(reflectionUtils.isAssignableFrom(field2.getGenericType(), field1.getGenericType())).isTrue();
    }

    @Test
    void shouldTrueOnSameAssignableGenericTypes() {
        Field field1 = reflectionUtils.findField(AssignableList.class, "charSequences", false)
                .orElseThrow();
        assertThat(reflectionUtils.isAssignableFrom(field1.getGenericType(), Object.class)).isTrue();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class AssignableList {

        private List<CharSequence> charSequences;
        private LinkedList<String> strings;

    }

}
