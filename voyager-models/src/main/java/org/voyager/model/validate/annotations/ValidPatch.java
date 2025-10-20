package org.voyager.model.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.model.validate.PatchValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PatchValidator.class)
public @interface ValidPatch {
    String message() default "Must include at least one valid field";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
