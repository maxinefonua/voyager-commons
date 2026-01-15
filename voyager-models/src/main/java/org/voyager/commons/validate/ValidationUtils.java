package org.voyager.commons.validate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.ConstraintViolation;
import org.hibernate.validator.HibernateValidator;
import org.voyager.commons.error.ValidationException;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ValidationUtils {
    private static final Validator VALIDATOR;
    static {
        // suppress jakarta.persistence logging to SEVERE
        Logger jakartaLogger = Logger.getLogger("jakarta.persistence.spi");
        jakartaLogger.setLevel(Level.SEVERE);

        // Reduce Hibernate Validator logging to SEVERE
        Logger hibernateLogger = Logger.getLogger("org.hibernate.validator");
        hibernateLogger.setLevel(Level.SEVERE);

        try (ValidatorFactory factory = Validation.byProvider(HibernateValidator.class)
                .configure()
                .buildValidatorFactory()) {
            VALIDATOR = factory.getValidator();
        }
    }

    public static <T> Set<ConstraintViolation<T>> validate(T object) {
        return VALIDATOR.validate(object);
    }

    public static <T> void validateAndThrow(T object) {
        Set<ConstraintViolation<T>> violations = validate(object);
        if (!violations.isEmpty()) {
            @SuppressWarnings("unchecked")
            Set<ConstraintViolation<?>> wildcardSet = (Set<ConstraintViolation<?>>) (Set<?>) violations;

            throw ValidationException.builder()
                    .constraintViolationSet(wildcardSet)
                    .aClass(object.getClass())
                    .build();
        }
    }
}
