package org.voyager.model.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.regex.Pattern;

public class AllStringMatchRegexValidator implements ConstraintValidator<AllStringsMatchRegex, Collection<String>> {
    private Pattern pattern;

    @Override
    public void initialize(AllStringsMatchRegex constraintAnnotation) {
        // Compile the pattern once when validator is initialized
        this.pattern = Pattern.compile(constraintAnnotation.regexp());
    }

    @Override
    public boolean isValid(Collection<String> collection, ConstraintValidatorContext context) {
        if (collection == null) return true; // Allow null - use @NotNull if you want to reject null
        return collection.stream().noneMatch(
                str -> str == null || !pattern.matcher(str).matches());
    }
}
