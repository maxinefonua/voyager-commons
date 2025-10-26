package org.voyager.sdk.utils;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import java.util.Set;
import java.util.stream.Collectors;

public class JakartaValidationUtil {
    private static final Validator VALIDATOR;

    static {
        try (ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    public static Validator getValidator() {
        return VALIDATOR;
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
