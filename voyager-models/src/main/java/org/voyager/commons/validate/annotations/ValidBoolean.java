package org.voyager.commons.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.commons.validate.BooleanValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BooleanValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBoolean {
    boolean allowNull() default false;
    String message() default "must be a valid boolean string (true/false)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean caseSensitive() default true;
}
