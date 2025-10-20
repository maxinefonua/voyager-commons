package org.voyager.model.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.model.validate.annotations.ValidLatitude;

public class LatitudeValidator implements ConstraintValidator<ValidLatitude, String> {
    boolean allowNull;

    @Override
    public void initialize(ValidLatitude constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    // Regex for valid latitude: -90.0 to 90.0 with optional sign and decimals
    private static final String LATITUDE_PATTERN = "^[-+]?([1-8]?\\d(\\.\\d+)?|90(\\.0+)?)$";

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Handle null values
        if (value == null) {
            return allowNull;
        }
        // Check pattern match
        if (!value.matches(LATITUDE_PATTERN)) {
            return false;
        }
        // Additional validation: parse and check numeric range for extra safety
        try {
            double latitude = Double.parseDouble(value);
            return latitude >= -90.0 && latitude <= 90.0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
