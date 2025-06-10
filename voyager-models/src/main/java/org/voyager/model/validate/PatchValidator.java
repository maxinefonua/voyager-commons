package org.voyager.model.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.reflect.Field;

import static org.voyager.utils.MessageUtils.getIllegalAccessValidatorMessage;

public class PatchValidator implements ConstraintValidator<ValidPatch,Object> {
    @Override
    public boolean isValid(Object object, ConstraintValidatorContext constraintValidatorContext) {
        if (object.getClass().getFields().length > object.getClass().getDeclaredFields().length) return false;
        for (Field field : object.getClass().getDeclaredFields()) {
            boolean prevAcces = field.canAccess(object);
            field.setAccessible(true);
            try {
                if (field.get(object) != null) return true;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(getIllegalAccessValidatorMessage(field.getName(), object.getClass().getName()),e);
            } finally {
                field.setAccessible(prevAcces);
            }
        }
        return false;
    }
}
