package org.voyager.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
        String validLongitude = "174.10";
        assertTrue(longitudeValidator.isValid(validLongitude,context));

        validLongitude = "-110";
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

        invalidLongitude = "00.1009";
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));

        invalidLongitude = ".10-";
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));

        invalidLongitude = null;
        assertFalse(longitudeValidator.isValid(invalidLongitude,context));
    }
}