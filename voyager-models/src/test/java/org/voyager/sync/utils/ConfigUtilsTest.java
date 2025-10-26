package org.voyager.sync.utils;

import org.junit.jupiter.api.Test;
import org.voyager.commons.utils.Environment;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigUtilsTest {
    class ConstantsTestClass extends Environment {
        @Override
        protected String getEnvVariable(String key) {
            switch (key) {
                case "TEST_VAR":
                    return "${TEST_VAR}";
                case "SET_VAR":
                    return "value";
            }
            return System.getenv(key);
        }
    }

    @Test
    void validateEnvVariableList() {
        assertThrows(IllegalArgumentException.class,()->new ConstantsTestClass().validateEnvVars(List.of("")));
        ConstantsTestClass constants = new ConstantsTestClass();
        assertThrows(IllegalArgumentException.class,()->constants.validateEnvVars(List.of("TEST_VAR")));
        assertDoesNotThrow(()->constants.validateEnvVars(List.of("SET_VAR")));
    }
}