package org.voyager.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
}