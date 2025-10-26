package org.voyager.commons.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.commons.validate.AirportCodeCollectionValidator;
import java.lang.annotation.Documented;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Documented
@Constraint(validatedBy = AirportCodeCollectionValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAirportCodeCollection {
    boolean allowEmptyCollection();
    boolean allowNullCollection();
    boolean caseSensitive();
    String message();
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
