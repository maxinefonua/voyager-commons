package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.annotations.ValidFlightNumberCollection;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class FlightNumberCollectionValidatorTest {
    FlightNumberCollectionValidator flightNumberCollectionValidator;

    @BeforeEach
    void setUp() {
        flightNumberCollectionValidator = new FlightNumberCollectionValidator();
    }

    @Test
    void initialize() {
        ValidFlightNumberCollection flightNumberCollection = new ValidFlightNumberCollection(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
            }

            @Override
            public boolean allowEmptyCollection() {
                return false;
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
        flightNumberCollectionValidator.initialize(flightNumberCollection);
        assertFalse(flightNumberCollectionValidator.isValid(List.of("em","ts"),mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberCollectionValidator.isValid(List.of(),mock(ConstraintValidatorContext.class)));
        List<String> codes = new ArrayList<>();
        codes.add(null);
        assertFalse(flightNumberCollectionValidator.isValid(codes,mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberCollectionValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberCollectionValidator.isValid(List.of(""),mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberCollectionValidator.isValid(List.of("elq12"),mock(ConstraintValidatorContext.class)));
        assertTrue(flightNumberCollectionValidator.isValid(List.of("ELQ12"),mock(ConstraintValidatorContext.class)));
    }

    @Test
    void isValid() {
        ValidFlightNumberCollection flightNumberCollection = new ValidFlightNumberCollection(){
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
        flightNumberCollectionValidator.initialize(flightNumberCollection);
        assertFalse(flightNumberCollectionValidator.isValid(List.of("em","ts"),mock(ConstraintValidatorContext.class)));
        assertTrue(flightNumberCollectionValidator.isValid(List.of(),mock(ConstraintValidatorContext.class)));
        List<String> codes = new ArrayList<>();
        codes.add(null);
        assertFalse(flightNumberCollectionValidator.isValid(codes,mock(ConstraintValidatorContext.class)));
        assertTrue(flightNumberCollectionValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberCollectionValidator.isValid(List.of(""),mock(ConstraintValidatorContext.class)));
        assertFalse(flightNumberCollectionValidator.isValid(List.of("elq12"),mock(ConstraintValidatorContext.class)));
        assertTrue(flightNumberCollectionValidator.isValid(List.of("ELQ12"),mock(ConstraintValidatorContext.class)));
    }
}