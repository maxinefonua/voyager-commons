package org.voyager.model.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.model.validate.annotations.ValidLatitude;
import org.voyager.model.validate.annotations.ValidLongitude;

public class LongitudeValidator implements ConstraintValidator<ValidLongitude, String> {
    // Longitude regex: -180 to 180 with optional decimals
    private static final String LONGITUDE_PATTERN = "^[-+]?((1[0-7]\\d|[1-9]?\\d)(\\.\\d+)?|180(\\.0+)?)$";
    boolean allowNull;

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
        // Check pattern match
        if (!value.matches(LONGITUDE_PATTERN)) {
            return false;
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
