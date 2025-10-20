package org.voyager.utils;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConstantsTest {
    class ConstantsTestClass extends Constants {
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
        assertThrows(IllegalArgumentException.class,()->new Constants().validateEnvVars(List.of("")));
        ConstantsTestClass constants = new ConstantsTestClass();
        assertThrows(IllegalArgumentException.class,()->constants.validateEnvVars(List.of("TEST_VAR")));
        assertDoesNotThrow(()->constants.validateEnvVars(List.of("SET_VAR")));
    }
}