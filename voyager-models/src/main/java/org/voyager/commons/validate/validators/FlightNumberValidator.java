package org.voyager.commons.validate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidFlightNumber;

public class FlightNumberValidator implements ConstraintValidator<ValidFlightNumber, String> {
    boolean allowNull;
    @Override
    public void initialize(ValidFlightNumber constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) {
            return allowNull;
        }
        return s.matches(Regex.FLIGHT_NUMBER);
    }
}
