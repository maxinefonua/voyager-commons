package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidAirportCodeCollection;
import java.util.Collection;

public class AirportCodeCollectionValidator implements ConstraintValidator<ValidAirportCodeCollection, Collection<String>> {
    private boolean allowNullCollection;
    private boolean allowEmptyCollection;
    private boolean caseSensitive;


    @Override
    public void initialize(ValidAirportCodeCollection constraintAnnotation) {
        this.caseSensitive = constraintAnnotation.caseSensitive();
        this.allowNullCollection = constraintAnnotation.allowNullCollection();
        this.allowEmptyCollection = constraintAnnotation.allowEmptyCollection();
    }

    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext context) {
        if (collection == null) {
            return allowNullCollection;
        }
        if (collection.isEmpty()) {
            return allowEmptyCollection;
        }
        if (caseSensitive) {
            return collection.stream().noneMatch(
                    str -> str == null || !str.matches(Regex.AIRPORT_CODE));
        }
        return collection.stream().noneMatch(
                str -> str == null || !str.matches(Regex.AIRPORT_CODE_CASE_INSENSITIVE));
    }
}
