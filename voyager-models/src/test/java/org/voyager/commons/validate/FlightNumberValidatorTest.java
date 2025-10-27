package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.annotations.ValidFlightNumber;
import org.voyager.commons.validate.validators.FlightNumberValidator;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class FlightNumberValidatorTest {
    FlightNumberValidator flightNumberValidator;

    @BeforeEach
    void setUp() {
        flightNumberValidator = new FlightNumberValidator();
    }

    @Test
    void initialize() {
        ValidFlightNumber validFlightNumber = new ValidFlightNumber(){
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
        };

        flightNumberValidator.initialize(validFlightNumber);
        assertFalse(flightNumberValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("",mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("en",mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("jl1",mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("AA",mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("12",mock(ConstraintValidatorContext.class)));
        assertTrue(flightNumberValidator.isValid("JL1",mock(ConstraintValidatorContext.class)));
    }

    @Test
    void isValid() {
        ValidFlightNumber validFlightNumber = new ValidFlightNumber(){
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
        };

        flightNumberValidator.initialize(validFlightNumber);
        assertTrue(flightNumberValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("",mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("en",mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("jl1",mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("AA",mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberValidator.isValid("12",mock(ConstraintValidatorContext.class)));
        assertTrue(flightNumberValidator.isValid("JL1",mock(ConstraintValidatorContext.class)));
    }
}