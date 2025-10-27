package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.validators.BooleanValidator;
import org.voyager.commons.validate.annotations.ValidBoolean;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class BooleanValidatorTest {
    BooleanValidator booleanValidator;

    @BeforeEach
    void setup() {
        booleanValidator = new BooleanValidator();
    }


    @Test
    void initialize() {
        ValidBoolean validBoolean = new ValidBoolean(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public boolean allowNull() {
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

            @Override
            public boolean caseSensitive() {
                return false;
            }
        };
        booleanValidator.initialize(validBoolean);
        assertFalse(booleanValidator.isValid("",mock(ConstraintValidatorContext.class)));
        assertFalse(booleanValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(booleanValidator.isValid("test",mock(ConstraintValidatorContext.class)));
        assertTrue(booleanValidator.isValid("TRUE",mock(ConstraintValidatorContext.class)));
        assertTrue(booleanValidator.isValid("false",mock(ConstraintValidatorContext.class)));
    }

    @Test
    void isValid() {
        ValidBoolean validBoolean = new ValidBoolean(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public boolean allowNull() {
                return true;
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

            @Override
            public boolean caseSensitive() {
                return true;
            }
        };
        booleanValidator.initialize(validBoolean);
        assertFalse(booleanValidator.isValid("",mock(ConstraintValidatorContext.class)));
        assertTrue(booleanValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(booleanValidator.isValid("test",mock(ConstraintValidatorContext.class)));
        assertFalse(booleanValidator.isValid("TRUE",mock(ConstraintValidatorContext.class)));
        assertTrue(booleanValidator.isValid("false",mock(ConstraintValidatorContext.class)));
    }
}