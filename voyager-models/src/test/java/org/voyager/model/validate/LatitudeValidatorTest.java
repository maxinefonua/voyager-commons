package org.voyager.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;

class LatitudeValidatorTest {
    @Mock
    private ConstraintValidatorContext context;
    private LatitudeValidator latitudeValidator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        latitudeValidator = new LatitudeValidator();
    }

    @Test
    void isValid() {
        String validLatitude = "0.10";
        assertTrue(latitudeValidator.isValid(validLatitude,context));

        validLatitude = "10";
        assertTrue(latitudeValidator.isValid(validLatitude,context));
    }

    @Test
    void isInvalid() {
        String invalidLatitude = "0.10.";
        assertFalse(latitudeValidator.isValid(invalidLatitude,context));

        invalidLatitude = "";
        assertFalse(latitudeValidator.isValid(invalidLatitude,context));

        invalidLatitude = "100";
        assertFalse(latitudeValidator.isValid(invalidLatitude,context));

        invalidLatitude = "mas";
        assertFalse(latitudeValidator.isValid(invalidLatitude,context));

        invalidLatitude = "-100";
        assertFalse(latitudeValidator.isValid(invalidLatitude,context));

        invalidLatitude = ".10-";
        assertFalse(latitudeValidator.isValid(invalidLatitude,context));

        invalidLatitude = null;
        assertFalse(latitudeValidator.isValid(invalidLatitude,context));
    }
}