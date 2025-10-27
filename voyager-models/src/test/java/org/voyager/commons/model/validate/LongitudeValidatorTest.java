package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voyager.commons.validate.validators.LongitudeStringValidator;
import org.voyager.commons.validate.annotations.ValidLongitude;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class LongitudeValidatorTest {
    @Mock
    private ConstraintValidatorContext context;
    private LongitudeStringValidator longitudeStringValidator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        longitudeStringValidator = new LongitudeStringValidator();
    }

    @Test
    void isValid() {
        String validLongitude = "180";
        assertTrue(longitudeStringValidator.isValid(validLongitude,context));

        validLongitude = "-180";
        assertTrue(longitudeStringValidator.isValid(validLongitude,context));

        validLongitude = "-10.30";
        assertTrue(longitudeStringValidator.isValid(validLongitude,context));

        validLongitude = "00.1009";
        assertTrue(longitudeStringValidator.isValid(validLongitude,context));
    }

    @Test
    void isInvalid() {
        String invalidLongitude = "0.10.";
        assertFalse(longitudeStringValidator.isValid(invalidLongitude,context));

        invalidLongitude = "";
        assertFalse(longitudeStringValidator.isValid(invalidLongitude,context));

        invalidLongitude = "190";
        assertFalse(longitudeStringValidator.isValid(invalidLongitude,context));

        invalidLongitude = "mas";
        assertFalse(longitudeStringValidator.isValid(invalidLongitude,context));

        invalidLongitude = "-200";
        assertFalse(longitudeStringValidator.isValid(invalidLongitude,context));

        invalidLongitude = ".10-";
        assertFalse(longitudeStringValidator.isValid(invalidLongitude,context));

        invalidLongitude = null;
        assertFalse(longitudeStringValidator.isValid(invalidLongitude,context));
    }

    @Test
    void testInitialize() {
        ValidLongitude validLongitude = new ValidLongitude(){
            @Override
            public Class<? extends Annotation> annotationType() {
                return null;
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
            public boolean allowNull() {
                return true;
            }
        };
        longitudeStringValidator.initialize(validLongitude);
        assertTrue(longitudeStringValidator.isValid(null,context));
    }
}