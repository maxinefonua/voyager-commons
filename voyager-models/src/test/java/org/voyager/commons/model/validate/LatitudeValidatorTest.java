package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintTarget;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voyager.commons.validate.validators.LatitudeStringValidator;
import org.voyager.commons.validate.annotations.ValidLatitude;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class LatitudeValidatorTest {
    @Mock
    private ConstraintValidatorContext context;

    private LatitudeStringValidator latitudeStringValidator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        latitudeStringValidator = new LatitudeStringValidator();
    }

    @Test
    void isValid() {
        String validLatitude = "0.10";
        assertTrue(latitudeStringValidator.isValid(validLatitude,context));

        validLatitude = "90";
        assertTrue(latitudeStringValidator.isValid(validLatitude,context));
        validLatitude = "-90";
        assertTrue(latitudeStringValidator.isValid(validLatitude,context));
    }

    @Test
    void isInvalid() {
        String invalidLatitude = "0.10.";
        assertFalse(latitudeStringValidator.isValid(invalidLatitude,context));

        invalidLatitude = "";
        assertFalse(latitudeStringValidator.isValid(invalidLatitude,context));

        invalidLatitude = "100";
        assertFalse(latitudeStringValidator.isValid(invalidLatitude,context));

        invalidLatitude = "mas";
        assertFalse(latitudeStringValidator.isValid(invalidLatitude,context));

        invalidLatitude = "-100";
        assertFalse(latitudeStringValidator.isValid(invalidLatitude,context));

        invalidLatitude = ".10-";
        assertFalse(latitudeStringValidator.isValid(invalidLatitude,context));

        invalidLatitude = null;
        assertFalse(latitudeStringValidator.isValid(invalidLatitude,context));
        invalidLatitude = null;
        assertFalse(latitudeStringValidator.isValid(invalidLatitude,context));
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

        latitudeStringValidator.initialize(validLatitude);
        assertTrue(latitudeStringValidator.isValid(null,context));
    }
}