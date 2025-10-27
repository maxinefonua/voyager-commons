package org.voyager.commons.validate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.ValidZoneId;
import java.time.DateTimeException;
import java.time.ZoneId;

public class ZoneIdValidator  implements ConstraintValidator<ValidZoneId, String> {
    boolean allowNull;

    @Override
    public void initialize(ValidZoneId constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
            return allowNull;
        }
        try {
            ZoneId ignored = ZoneId.of(value);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
}
