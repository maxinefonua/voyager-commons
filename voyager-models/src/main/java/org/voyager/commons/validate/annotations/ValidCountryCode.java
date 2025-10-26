package org.voyager.commons.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.CountryCodeValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CountryCodeValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCountryCode {
    boolean allowNull() default false;
    String message() default Regex.ConstraintMessage.COUNTRY_CODE;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean caseSensitive() default true;
}
