package org.voyager.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PatchValidatorTest {
    @Mock
    private ConstraintValidatorContext context;

    private PatchValidator patchValidator;

    @Builder
    private static class TestClass {
        Object field1;
        Object field2;
        Object field3;
    }


    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        patchValidator = new PatchValidator();
    }

    @Test
    void isValid() {
        TestClass validObject = TestClass.builder().field1(new Object()).build();
        assertTrue(patchValidator.isValid(validObject,context));
    }

    @Test
    void isNotValid() {
        TestClass invalidObject = TestClass.builder().build();
        assertFalse(patchValidator.isValid(invalidObject,context));
    }
}