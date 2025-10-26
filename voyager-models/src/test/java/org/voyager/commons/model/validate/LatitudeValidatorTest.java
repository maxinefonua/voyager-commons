package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voyager.commons.validate.LatitudeValidator;
import org.voyager.commons.validate.annotations.ValidLatitude;

import java.lang.annotation.Annotation;

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

        validLatitude = "90";
        assertTrue(latitudeValidator.isValid(validLatitude,context));
        validLatitude = "-90";
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
        invalidLatitude = null;
        assertFalse(latitudeValidator.isValid(invalidLatitude,context));
    }

    @Test
    void nullAllowed() {
        ValidLatitude validLatitude = new ValidLatitude(){
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

        latitudeValidator.initialize(validLatitude);
        assertTrue(latitudeValidator.isValid(null,context));
    }
}