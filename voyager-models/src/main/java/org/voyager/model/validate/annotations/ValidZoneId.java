package org.voyager.model.validate.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.voyager.model.validate.ZoneIdValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ZoneIdValidator.class)
@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidZoneId {
    String message() default "must be a valid region time zone (e.g., Pacific/Honolulu)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    boolean allowNull() default false;
}
