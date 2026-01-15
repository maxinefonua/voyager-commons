package org.voyager.commons.error;

import jakarta.validation.Constraint;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.ValidationUtils;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ValidationExceptionTest {
    @Builder @Data
    static class TestClass {
        @Max(10)
        Integer testInteger;
        @NotNull
        Boolean testBoolean;
        String anyString;
    }
    @Test
    void builder() {
        TestClass testClass = TestClass.builder().build();
        Set<ConstraintViolation<TestClass>> constraintViolationSet = ValidationUtils.validate(testClass);

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<?>> wildcardSet = (Set<ConstraintViolation<?>>) (Set<?>) constraintViolationSet;
        ValidationException validationException = ValidationException.builder()
                .constraintViolationSet(wildcardSet)
                .aClass(TestClass.class)
                .build();
        assertEquals(TestClass.class,validationException.getAClass());
        assertEquals(constraintViolationSet,validationException.getConstraintViolationSet());
        String message = validationException.getMessage();
        assertEquals("TestClass invalid: 'testBoolean' must not be null",message);
    }

    @Test
    void getMessage() {
        TestClass testClass = TestClass.builder().testInteger(100).build();
        Set<ConstraintViolation<TestClass>> constraintViolationSet = ValidationUtils.validate(testClass);

        @SuppressWarnings("unchecked")
        Set<ConstraintViolation<?>> wildcardSet = (Set<ConstraintViolation<?>>) (Set<?>) constraintViolationSet;
        ValidationException validationException = ValidationException.builder()
                .constraintViolationSet(wildcardSet)
                .aClass(TestClass.class)
                .build();
        assertEquals(constraintViolationSet,validationException.getConstraintViolationSet());
        String message = validationException.getMessage();
        assertTrue(message.contains("'testInteger' must be less than or equal to 10"));
        assertTrue(message.contains("'testBoolean' must not be null"));
    }
}