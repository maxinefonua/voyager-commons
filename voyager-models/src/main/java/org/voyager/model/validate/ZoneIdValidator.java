package org.voyager.model.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.model.validate.annotations.ValidLongitude;
import org.voyager.model.validate.annotations.ValidZoneId;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.time.zone.ZoneRulesException;

public class ZoneIdValidator  implements ConstraintValidator<ValidZoneId, String> {
    boolean allowNull;

    @Override
    public void initialize(ValidZoneId constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) return allowNull;
        try {
            ZoneId.of(value);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
}
