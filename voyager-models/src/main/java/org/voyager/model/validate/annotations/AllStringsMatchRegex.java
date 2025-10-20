package org.voyager.model.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.model.validate.AllStringMatchRegexValidator;

import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = AllStringMatchRegexValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface AllStringsMatchRegex {
    String message() default "all elements must match regex";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    // The regex pattern to match
    String regexp();
}
