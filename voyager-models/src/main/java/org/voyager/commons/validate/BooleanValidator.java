package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.ValidBoolean;
import java.util.Arrays;
import java.util.List;

public class BooleanValidator implements ConstraintValidator<ValidBoolean, String> {
    private boolean allowNull;
    private boolean caseSensitive;
    private final List<String> validValues = Arrays.asList("true", "false");

    @Override
    public void initialize(ValidBoolean constraintAnnotation) {
        this.caseSensitive = constraintAnnotation.caseSensitive();
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }

        if (caseSensitive) {
            return validValues.contains(value);
        } else {
            return validValues.contains(value.toLowerCase());
        }
    }
}