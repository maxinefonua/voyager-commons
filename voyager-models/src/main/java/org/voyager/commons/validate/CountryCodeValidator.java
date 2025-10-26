package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidCountryCode;

public class CountryCodeValidator implements ConstraintValidator<ValidCountryCode, String> {
    private boolean allowNull;
    private boolean caseSensitive;

    @Override
    public void initialize(ValidCountryCode constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return allowNull;
        }
        if (caseSensitive) {
            return s.matches(Regex.COUNTRY_CODE);
        }
        return s.matches(Regex.COUNTRY_CODE_CASE_INSENSITIVE);
    }
}
