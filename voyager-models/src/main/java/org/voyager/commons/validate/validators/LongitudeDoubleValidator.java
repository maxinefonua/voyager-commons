package org.voyager.commons.validate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.ValidLongitude;

public class LongitudeDoubleValidator implements ConstraintValidator<ValidLongitude, Double> {
    private boolean allowNull;

    @Override
    public void initialize(ValidLongitude constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Double longitude, ConstraintValidatorContext context) {
        // Handle null values
        if (longitude == null) {
            return allowNull;
        }
        return longitude >= -180.0 && longitude <= 180.0;
    }
}