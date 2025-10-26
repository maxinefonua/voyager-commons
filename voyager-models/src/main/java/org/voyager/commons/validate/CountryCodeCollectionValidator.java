package org.voyager.commons.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidCountryCodeCollection;
import java.util.Collection;

public class CountryCodeCollectionValidator implements ConstraintValidator<ValidCountryCodeCollection, Collection<String>> {
    boolean allowNullCollection;
    boolean allowEmptyCollection;
    boolean caseSensitive;

    @Override
    public void initialize(ValidCountryCodeCollection constraintAnnotation) {
        this.allowEmptyCollection = constraintAnnotation.allowEmptyCollection();
        this.allowNullCollection = constraintAnnotation.allowNullCollection();
        this.caseSensitive = constraintAnnotation.caseSensitive();
    }

    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext constraintValidatorContext) {
        if (collection == null) {
            return allowNullCollection;
        }
        if (collection.isEmpty()) {
            return allowEmptyCollection;
        }
        if (caseSensitive) {
            return collection.stream().noneMatch(
                    str -> str == null || !str.matches(Regex.COUNTRY_CODE));
        }
        return collection.stream().noneMatch(
                str -> str == null || !str.matches(Regex.COUNTRY_CODE_CASE_INSENSITIVE));
    }
}
