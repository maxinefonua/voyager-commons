package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.ValidEnum;
import java.util.Arrays;
import java.util.List;

public class EnumPatchValidator implements ConstraintValidator<ValidEnum,String> {
    private List<String> allowedValues;
    private boolean allowNull;

    @Override
    public void initialize(ValidEnum constraintAnnotation) {
        this.allowNull = constraintAnnotation.allowNull();
        allowedValues = Arrays.stream(constraintAnnotation.enumClass().getEnumConstants())
                .map(Enum::name).toList();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext constraintValidatorContext) {
        if (value == null) {
           return allowNull;
        }
        return allowedValues.contains(value.toUpperCase()); // enables case insensitive validation
    }
}
