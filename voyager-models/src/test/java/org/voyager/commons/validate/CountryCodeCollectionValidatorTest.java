package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.annotations.ValidCountryCodeCollection;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CountryCodeCollectionValidatorTest {
    CountryCodeCollectionValidator countryCodeCollectionValidator;

    @BeforeEach
    void setUp() {
        countryCodeCollectionValidator = new CountryCodeCollectionValidator();
    }

    @Test
    void initialize() {
        ValidCountryCodeCollection validCountryCodeCollection = new ValidCountryCodeCollection(){
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
            public boolean caseSensitive() {
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
        countryCodeCollectionValidator.initialize(validCountryCodeCollection);
        assertTrue(countryCodeCollectionValidator.isValid(List.of("em","ts"),mock(ConstraintValidatorContext.class)));
        List<String> codes = new ArrayList<>();
        codes.add(null);
        assertFalse(countryCodeCollectionValidator.isValid(codes,mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeCollectionValidator.isValid(List.of(),mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeCollectionValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeCollectionValidator.isValid(List.of(""),mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeCollectionValidator.isValid(List.of("elq"),mock(ConstraintValidatorContext.class)));
    }

    @Test
    void isValid() {
        ValidCountryCodeCollection validCountryCodeCollection = new ValidCountryCodeCollection(){
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
            public boolean caseSensitive() {
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
        countryCodeCollectionValidator.initialize(validCountryCodeCollection);
        assertFalse(countryCodeCollectionValidator.isValid(List.of("em","ts"),mock(ConstraintValidatorContext.class)));
        assertTrue(countryCodeCollectionValidator.isValid(List.of(),mock(ConstraintValidatorContext.class)));
        List<String> codes = new ArrayList<>();
        codes.add(null);
        assertFalse(countryCodeCollectionValidator.isValid(codes,mock(ConstraintValidatorContext.class)));
        assertTrue(countryCodeCollectionValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeCollectionValidator.isValid(List.of(""),mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeCollectionValidator.isValid(List.of("elq"),mock(ConstraintValidatorContext.class)));
    }
}