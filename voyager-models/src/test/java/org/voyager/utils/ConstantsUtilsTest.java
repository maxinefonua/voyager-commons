package org.voyager.utils;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstantsUtilsTest {
    @Test
    void validateSystemProperty() {
        String propertyName = "TEST_VAR";
        String envVarName = "${TEST_VAR}";
        String propertyValue = "value";
        System.setProperty(propertyName,propertyValue);
        ConstantsUtils.validateSystemProperty(List.of(propertyName));
        System.clearProperty(propertyName);
        assertThrows(IllegalArgumentException.class,()->ConstantsUtils.validateSystemProperty(List.of(propertyName)));
        System.setProperty(propertyName,envVarName);
        assertThrows(IllegalArgumentException.class,()->ConstantsUtils.validateSystemProperty(List.of(propertyName)));
    }
}