package org.voyager.model.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.model.validate.annotations.NonNullElements;

import java.util.Collection;
import java.util.Objects;

public class NonNullElementsValidator implements ConstraintValidator<NonNullElements, Collection<?>> {

    @Override
    public boolean isValid(Collection<?> collection, ConstraintValidatorContext constraintValidatorContext) {
        if (collection == null) return true; // allows null collection
        if (collection.isEmpty()) return true; // allows empty collection
        return collection.stream().noneMatch(Objects::isNull);
    }
}
