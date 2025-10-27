package org.voyager.commons.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.validators.FlightNumberValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = FlightNumberValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidFlightNumber {
    boolean allowNull() default false;
    String message() default Regex.ConstraintMessage.FLIGHT_NUMBER;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
