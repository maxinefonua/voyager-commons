package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.EnumPatchValidator;
import org.voyager.commons.validate.annotations.ValidEnum;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.Mockito.mock;

class EnumPatchValidatorTest {

    public enum TestEnum {
        ACTIVE,
        INACTIVE,
        PENDING
    }

    @Test
    void testInitialize() {
        ValidEnum validEnumAnnotation = new ValidEnum() {
            @Override
            public Class<? extends Enum<?>> enumClass() {
                return TestEnum.class;
            }

            @Override
            public String message() {
                return "Invalid enum value";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public boolean allowNull() {
                return false;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ValidEnum.class;
            }
        };

        EnumPatchValidator enumPatchValidator = new EnumPatchValidator();
        enumPatchValidator.initialize(validEnumAnnotation);

        // Then
        assertTrue(enumPatchValidator.isValid("ACTIVE",mock(ConstraintValidatorContext.class)));
        assertTrue(enumPatchValidator.isValid("INACTIVE",mock(ConstraintValidatorContext.class)));
        assertTrue(enumPatchValidator.isValid("PENDING",mock(ConstraintValidatorContext.class)));
        assertFalse(enumPatchValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(enumPatchValidator.isValid("TEST",mock(ConstraintValidatorContext.class)));
    }

    @Test
    void testInitializeOpposite() {
        ValidEnum validEnumAnnotation = new ValidEnum() {
            @Override
            public Class<? extends Enum<?>> enumClass() {
                return TestEnum.class;
            }

            @Override
            public String message() {
                return "Invalid enum value";
            }

            @Override
            public Class<?>[] groups() {
                return new Class[0];
            }

            @Override
            @SuppressWarnings("unchecked")
            public Class<? extends jakarta.validation.Payload>[] payload() {
                return new Class[0];
            }

            @Override
            public boolean allowNull() {
                return true;
            }

            @Override
            public Class<? extends Annotation> annotationType() {
                return ValidEnum.class;
            }
        };

        EnumPatchValidator enumPatchValidator = new EnumPatchValidator();
        enumPatchValidator.initialize(validEnumAnnotation);

        // Then
        assertTrue(enumPatchValidator.isValid("ACTIVE",mock(ConstraintValidatorContext.class)));
        assertTrue(enumPatchValidator.isValid("INACTIVE",mock(ConstraintValidatorContext.class)));
        assertTrue(enumPatchValidator.isValid("PENDING",mock(ConstraintValidatorContext.class)));
        assertTrue(enumPatchValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(enumPatchValidator.isValid("TEST",mock(ConstraintValidatorContext.class)));
    }
}