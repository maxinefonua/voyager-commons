package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.ValidLongitude;

public class LongitudeValidator implements ConstraintValidator<ValidLongitude, String> {
    private boolean allowNull;

    @Override
    public void initialize(ValidLongitude constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Handle null values
        if (value == null) {
            return allowNull;
        }
        // Additional validation: parse and check numeric range for extra safety
        try {
            double longitude = Double.parseDouble(value);
            return longitude >= -180.0 && longitude <= 180.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
