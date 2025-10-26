package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.validate.annotations.NonNullElements;
import java.util.Collection;
import java.util.Objects;

public class NonNullElementsValidator implements ConstraintValidator<NonNullElements, Collection<?>> {
    boolean allowNullCollection;
    boolean allowEmptyCollection;

    @Override
    public void initialize(NonNullElements constraintAnnotation) {
        this.allowNullCollection = constraintAnnotation.allowNullCollection();
        this.allowEmptyCollection = constraintAnnotation.allowEmptyCollection();
    }

    @Override
    public boolean isValid(Collection<?> collection, ConstraintValidatorContext constraintValidatorContext) {
        if (collection == null) {
            return allowNullCollection;
        }
        if (collection.isEmpty()) {
            return allowEmptyCollection;
        }
        return collection.stream().noneMatch(Objects::isNull);
    }
}
