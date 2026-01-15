package org.voyager.commons.error;

import jakarta.validation.ConstraintViolation;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;
import java.util.StringJoiner;

@Getter @Builder
public class ValidationException extends jakarta.validation.ValidationException {
    private final Set<ConstraintViolation<?>> constraintViolationSet;
    private final Class<?> aClass;

    @Override
    public String getMessage() {
        StringJoiner stringJoiner = new StringJoiner("; ");
        constraintViolationSet.forEach(constraintViolation ->
                stringJoiner.add(String.format("'%s' %s",
                        constraintViolation.getPropertyPath(),
                        constraintViolation.getMessage())));
        return String.format("%s invalid: %s",aClass.getSimpleName(),stringJoiner);
    }
}
