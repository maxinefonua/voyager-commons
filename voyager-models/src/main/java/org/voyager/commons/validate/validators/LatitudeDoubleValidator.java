package org.voyager.commons.validate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.ValidLatitude;

public class LatitudeDoubleValidator implements ConstraintValidator<ValidLatitude, Double> {
    boolean allowNull;

    @Override
    public void initialize(ValidLatitude constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(Double latitude, ConstraintValidatorContext context) {
        if (latitude == null) {
            return allowNull;
        }
        return latitude >= -90.0 && latitude <= 90.0;
    }
}