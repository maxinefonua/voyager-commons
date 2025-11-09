package org.voyager.commons.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.commons.validate.validators.PatchValidator;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PatchValidator.class)
public @interface ValidNonNullField {
    String message() default "must have at least one nonnull field";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
