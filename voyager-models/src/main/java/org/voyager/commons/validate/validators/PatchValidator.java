package org.voyager.commons.validate.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.ValidPatch;
import java.lang.reflect.Field;

public class PatchValidator implements ConstraintValidator<ValidPatch,Object> {
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object == null) return false;
        Field[] declaredFields = object.getClass().getDeclaredFields();
        for (Field field : declaredFields) {
            try {
                setFieldAccessible(field);
                if (field.get(object) != null) return true;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e.getMessage(),e);
            }
        }
        return false;
    }

    protected void setFieldAccessible(Field field) {
        field.setAccessible(true);
    }
}
