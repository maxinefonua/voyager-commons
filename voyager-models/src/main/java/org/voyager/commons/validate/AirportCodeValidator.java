package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidAirportCode;


public class AirportCodeValidator implements ConstraintValidator<ValidAirportCode, String> {
    private boolean allowNull;
    private boolean caseSensitive;

    @Override
    public void initialize(ValidAirportCode constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) return allowNull;
        if (caseSensitive) {
            return s.matches(Regex.AIRPORT_CODE);
        }
        return s.matches(Regex.AIRPORT_CODE_CASE_INSENSITIVE);
    }
}
