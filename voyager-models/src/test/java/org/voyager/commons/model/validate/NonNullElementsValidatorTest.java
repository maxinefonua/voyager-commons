package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.NonNullElementsValidator;
import org.voyager.commons.validate.annotations.NonNullElements;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class NonNullElementsValidatorTest {

    @Test
    void testInitialize() {
        NonNullElements nonNullElementsAnnotation = new NonNullElements() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public boolean allowEmptyCollection() {
                return true;
            }

            @Override
            public boolean allowNullCollection() {
                return false;
            }

            @Override
            public String message() {
                return "";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            public Class<? extends Payload>[] payload() {
                return new Class[0];
            }
        };

        NonNullElementsValidator nonNullElementsValidator = new NonNullElementsValidator();
        nonNullElementsValidator.initialize(nonNullElementsAnnotation);

        assertFalse(nonNullElementsValidator.isValid(null,mock(ConstraintValidatorContext.class)));

        // Then
        List<String> test = new ArrayList<>();
        assertTrue(nonNullElementsValidator.isValid(test,mock(ConstraintValidatorContext.class)));
        test.add("valid");
        assertTrue(nonNullElementsValidator.isValid(test,mock(ConstraintValidatorContext.class)));
        test.add(null);
        assertFalse(nonNullElementsValidator.isValid(test,mock(ConstraintValidatorContext.class)));
        test.add("again");
        assertFalse(nonNullElementsValidator.isValid(test,mock(ConstraintValidatorContext.class)));
        test.remove(null);
        assertTrue(nonNullElementsValidator.isValid(test,mock(ConstraintValidatorContext.class)));
    }
}