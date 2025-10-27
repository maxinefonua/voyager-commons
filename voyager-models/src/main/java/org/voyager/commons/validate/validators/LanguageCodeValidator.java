package org.voyager.commons.validate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidLanguageCode;

public class LanguageCodeValidator implements ConstraintValidator<ValidLanguageCode, String> {
    boolean allowNull;

    @Override
    public void initialize(ValidLanguageCode constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }
        return value.matches(Regex.LANGUAGE_CODE);
    }
}