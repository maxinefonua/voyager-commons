package org.voyager.commons.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.CountryCodeValidator;
import org.voyager.commons.validate.FlightNumberValidator;

import java.lang.annotation.*;

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
