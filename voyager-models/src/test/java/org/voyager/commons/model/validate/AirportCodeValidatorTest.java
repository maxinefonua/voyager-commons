package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.validators.AirportCodeValidator;
import org.voyager.commons.validate.annotations.ValidAirportCode;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AirportCodeValidatorTest {
    AirportCodeValidator airportCodeValidator;

    @BeforeEach
    void setup() {
        airportCodeValidator = new AirportCodeValidator();
    }

    @Test
    void initVar1() {
        ValidAirportCode validAirportCode = new ValidAirportCode(){
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

        airportCodeValidator.initialize(validAirportCode);
        assertFalse(airportCodeValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(airportCodeValidator.isValid("",mock(ConstraintValidatorContext.class)));
        assertFalse(airportCodeValidator.isValid("test",mock(ConstraintValidatorContext.class)));
        assertTrue(airportCodeValidator.isValid("act",mock(ConstraintValidatorContext.class)));
        assertTrue(airportCodeValidator.isValid("OKC",mock(ConstraintValidatorContext.class)));
    }

    @Test
    void initVar2() {
        ValidAirportCode validAirportCode = new ValidAirportCode(){
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

        airportCodeValidator.initialize(validAirportCode);
        assertTrue(airportCodeValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(airportCodeValidator.isValid("",mock(ConstraintValidatorContext.class)));
        assertFalse(airportCodeValidator.isValid("test",mock(ConstraintValidatorContext.class)));
        assertFalse(airportCodeValidator.isValid("act",mock(ConstraintValidatorContext.class)));
        assertTrue(airportCodeValidator.isValid("OKC",mock(ConstraintValidatorContext.class)));
    }
}