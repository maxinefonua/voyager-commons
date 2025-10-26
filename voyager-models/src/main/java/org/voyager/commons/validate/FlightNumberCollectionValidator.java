package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidFlightNumberCollection;

import java.util.Collection;

public class FlightNumberCollectionValidator implements ConstraintValidator<ValidFlightNumberCollection,
        Collection<String>> {
    boolean allowNullCollection;
    boolean allowEmptyCollection;

    @Override
    public void initialize(ValidFlightNumberCollection constraintAnnotation) {
        this.allowEmptyCollection = constraintAnnotation.allowEmptyCollection();
        this.allowNullCollection = constraintAnnotation.allowNullCollection();
    }

    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext constraintValidatorContext) {
        if (collection == null) {
            return allowNullCollection;
        }
        if (collection.isEmpty()) {
            return allowEmptyCollection;
        }
        return collection.stream().noneMatch(
                str -> str == null || !str.matches(Regex.FLIGHT_NUMBER));
    }
}
