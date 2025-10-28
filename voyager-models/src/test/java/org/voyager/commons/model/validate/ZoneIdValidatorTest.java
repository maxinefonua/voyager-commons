package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voyager.commons.validate.validators.ZoneIdValidator;
import org.voyager.commons.validate.annotations.ValidZoneId;

import java.lang.annotation.Annotation;

import static org.junit.jupiter.api.Assertions.*;

class ZoneIdValidatorTest {
    @Mock
    private ConstraintValidatorContext context;
    private ZoneIdValidator zoneIdValidator;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        zoneIdValidator = new ZoneIdValidator();
    }

    @Test
    void isValid() {
        String valid = "Pacific/Tongatapu";
        assertTrue(zoneIdValidator.isValid(valid,context));

        valid = "Pacific/Honolulu";
        assertTrue(zoneIdValidator.isValid(valid,context));

        valid = "Asia/Tokyo";
        assertTrue(zoneIdValidator.isValid(valid,context));
    }

    @Test
    void isInvalid() {
        String invalid = "";
        assertFalse(zoneIdValidator.isValid(invalid,context));

        invalid = "America/California";
        assertFalse(zoneIdValidator.isValid(invalid,context));

        invalid = "testing";
        assertFalse(zoneIdValidator.isValid(invalid,context));

        invalid = null;
        assertFalse(zoneIdValidator.isValid(invalid,context));
    }

    @Test
    void initialize() {
        ValidZoneId validZoneId = new ValidZoneId(){
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

        zoneIdValidator.initialize(validZoneId);
        assertTrue(zoneIdValidator.isValid(null,context));
    }
}