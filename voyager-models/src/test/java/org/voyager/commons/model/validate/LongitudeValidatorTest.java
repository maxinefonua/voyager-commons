package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voyager.commons.validate.LongitudeValidator;
import org.voyager.commons.validate.annotations.ValidLongitude;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class LongitudeValidatorTest {
    @Mock
    private ConstraintValidatorContext context;
    private LongitudeValidator longitudeValidator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        longitudeValidator = new LongitudeValidator();
    }

    @Test
    void isValid() {
        String validLongitude = "180";
        assertTrue(longitudeValidator.isValid(validLongitude,context));

        validLongitude = "-180";
        assertTrue(longitudeValidator.isValid(validLongitude,context));

        validLongitude = "-10.30";
        assertTrue(longitudeValidator.isValid(validLongitude,context));

        validLongitude = "00.1009";
        assertTrue(longitudeValidator.isValid(validLongitude,context));
    }

    @Test
    void isInvalid() {
        String invalidLongitude = "0.10.";
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));

        invalidLongitude = "";
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));

        invalidLongitude = "190";
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));

        invalidLongitude = "mas";
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));

        invalidLongitude = "-200";
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));

        invalidLongitude = ".10-";
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));

        invalidLongitude = null;
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));
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
        longitudeValidator.initialize(validLongitude);
        assertTrue(longitudeValidator.isValid(null,context));
    }
}