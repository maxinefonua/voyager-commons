package org.voyager.utils;


import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import java.util.Set;
import java.util.stream.Collectors;

public class JakartaValidationUtil {

    public static Validator getValidator() {
        // Manually configure Hibernate Validator
        HibernateValidatorConfiguration configuration = Validation.byProvider(HibernateValidator.class)
                .configure();

        return configuration.buildValidatorFactory().getValidator();
    }

    public static <T> void validate(T object) {
        Validator validator = getValidator();
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (!violations.isEmpty()) {
            String errorMessage = violations.stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining("; "));
            throw new IllegalArgumentException("Validation failed: " + errorMessage);
        }
    }
}
