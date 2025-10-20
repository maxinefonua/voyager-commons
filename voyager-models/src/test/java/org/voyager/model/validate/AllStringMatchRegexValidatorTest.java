package org.voyager.model.validate;

import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.junit.jupiter.api.Test;
import org.voyager.model.validate.annotations.AllStringsMatchRegex;
import org.voyager.utils.Constants;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AllStringMatchRegexValidatorTest {

    @Test
    void isValid() {
        AllStringsMatchRegex allStringsMatchAnnotation = new AllStringsMatchRegex() {
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
            public String regexp() {
                return Constants.Voyager.Regex.IATA_CODE_ALPHA3;
            }
        };

        AllStringMatchRegexValidator allStringMatchRegexValidator = new AllStringMatchRegexValidator();
        allStringMatchRegexValidator.initialize(allStringsMatchAnnotation);

        // Then
        List<String> test = new ArrayList<>();
        assertTrue(allStringMatchRegexValidator.isValid(test,mock(ConstraintValidatorContext.class)));
        test.add("abc");
        assertTrue(allStringMatchRegexValidator.isValid(test,mock(ConstraintValidatorContext.class)));

        test.add("123");
        assertFalse(allStringMatchRegexValidator.isValid(test,mock(ConstraintValidatorContext.class)));

        test.remove("123");
        assertTrue(allStringMatchRegexValidator.isValid(test,mock(ConstraintValidatorContext.class)));

        test.add("em");
        assertFalse(allStringMatchRegexValidator.isValid(test,mock(ConstraintValidatorContext.class)));

        test.remove("em");
        assertTrue(allStringMatchRegexValidator.isValid(test,mock(ConstraintValidatorContext.class)));
    }
}