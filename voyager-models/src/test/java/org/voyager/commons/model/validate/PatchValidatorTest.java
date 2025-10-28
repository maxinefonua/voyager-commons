package org.voyager.commons.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import lombok.Builder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voyager.commons.validate.validators.PatchValidator;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        assertFalse(patchValidator.isValid(null,context));
        TestClass invalidObject = TestClass.builder().build();
        assertFalse(patchValidator.isValid(invalidObject,context));
        invalidObject = TestClass.builder().field2(null).build();
        assertFalse(patchValidator.isValid(invalidObject,context));
    }

    @Test
    void isValidThrowsException() throws Exception {
        class PatchValidatorTestClass extends PatchValidator {
            @Override
            protected void setFieldAccessible(Field field) {
                // do nothing to trigger exception
            }
        }
        Object testObject = new Object() {
            @SuppressWarnings("unused")
            private String field1 = null;
            @SuppressWarnings("unused")
            private String field2 = "hasValue";
        };
        assertThrows(RuntimeException.class,() -> new PatchValidatorTestClass().isValid(testObject, null));
    }
}