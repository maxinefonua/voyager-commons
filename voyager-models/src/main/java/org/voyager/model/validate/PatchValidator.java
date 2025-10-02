package org.voyager.model.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.lang.reflect.Field;

public class PatchValidator implements ConstraintValidator<ValidPatch,Object> {
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object == null) return false;
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                if (field.get(object) != null) return true;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(),e);
            }
        }
        return false;
    }
}
