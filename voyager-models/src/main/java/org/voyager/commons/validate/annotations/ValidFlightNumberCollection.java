package org.voyager.commons.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.commons.validate.FlightNumberCollectionValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = FlightNumberCollectionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFlightNumberCollection {
    boolean allowEmptyCollection();
    boolean allowNullCollection();
    String message();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
