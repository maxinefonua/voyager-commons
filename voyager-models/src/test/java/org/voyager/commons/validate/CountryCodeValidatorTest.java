package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.annotations.ValidCountryCode;
import org.voyager.commons.validate.validators.CountryCodeValidator;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CountryCodeValidatorTest {
    CountryCodeValidator countryCodeValidator;

    @BeforeEach
    void setUp() {
        countryCodeValidator = new CountryCodeValidator();
    }

    @Test
    void initialize() {
        ValidCountryCode validCountryCode = new ValidCountryCode(){
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

        countryCodeValidator.initialize(validCountryCode);
        assertTrue(countryCodeValidator.isValid("co",mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeValidator.isValid("",mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeValidator.isValid("cot",mock(ConstraintValidatorContext.class)));
    }

    @Test
    void isValid() {
        ValidCountryCode validCountryCode = new ValidCountryCode(){
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

        countryCodeValidator.initialize(validCountryCode);
        assertFalse(countryCodeValidator.isValid("co",mock(ConstraintValidatorContext.class)));
        assertTrue(countryCodeValidator.isValid(null,mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeValidator.isValid("",mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeValidator.isValid("cot",mock(ConstraintValidatorContext.class)));
        assertFalse(countryCodeValidator.isValid("COT",mock(ConstraintValidatorContext.class)));
        assertTrue(countryCodeValidator.isValid("TO",mock(ConstraintValidatorContext.class)));
    }
}