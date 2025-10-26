package org.voyager.commons.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.commons.validate.NonNullElementsValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = NonNullElementsValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NonNullElements {
    boolean allowEmptyCollection() default false;
    boolean allowNullCollection() default true;
    String message() default "must not contain null elements";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
