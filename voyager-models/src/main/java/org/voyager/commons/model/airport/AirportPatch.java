package org.voyager.commons.model.airport;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidPatch;

@Builder @Data
@AllArgsConstructor
@NoArgsConstructor @ValidPatch
@ToString(includeFieldNames = false)
public class AirportPatch {
    @Pattern(regexp = Regex.NONEMPTY_TRIMMED,
            message = Regex.ConstraintMessage.NONEMPTY_TRIMMED)
    private String name;

    @Pattern(regexp = Regex.NONEMPTY_TRIMMED,
            message = Regex.ConstraintMessage.NONEMPTY_TRIMMED)
    private String city;

    @Pattern(regexp = Regex.NONEMPTY_TRIMMED,
            message = Regex.ConstraintMessage.NONEMPTY_TRIMMED)
    private String subdivision;

    @ValidEnum(enumClass = AirportType.class,
            allowNull = true)
    private String type;
}
