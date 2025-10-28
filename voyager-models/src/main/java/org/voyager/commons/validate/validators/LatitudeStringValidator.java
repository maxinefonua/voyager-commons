package org.voyager.commons.validate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.ValidLatitude;

public class LatitudeStringValidator implements ConstraintValidator<ValidLatitude, String> {
    boolean allowNull;

    @Override
    public void initialize(ValidLatitude constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return allowNull;
        }
        try {
            double latitude = Double.parseDouble(value);
            return latitude >= -90.0 && latitude <= 90.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
