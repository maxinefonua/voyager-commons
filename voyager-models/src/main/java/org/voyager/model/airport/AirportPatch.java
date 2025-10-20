package org.voyager.model.airport;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.model.validate.annotations.ValidEnum;
import org.voyager.model.validate.annotations.ValidPatch;
import org.voyager.utils.Constants;

@Builder @Data
@AllArgsConstructor
@NoArgsConstructor @ValidPatch
@ToString(includeFieldNames = false)
public class AirportPatch {
    @Pattern(regexp = Constants.Voyager.Regex.NOEMPTY_NOWHITESPACE,
            message = Constants.Voyager.ConstraintMessage.NOEMPTY_NOWHITESPACE)
    private String name;

    @Pattern(regexp = Constants.Voyager.Regex.NOEMPTY_NOWHITESPACE,
            message = Constants.Voyager.ConstraintMessage.NOEMPTY_NOWHITESPACE)
    private String city;

    @Pattern(regexp = Constants.Voyager.Regex.NOEMPTY_NOWHITESPACE,
            message = Constants.Voyager.ConstraintMessage.NOEMPTY_NOWHITESPACE)
    private String subdivision;

    @ValidEnum(enumClass = AirportType.class)
    private String type;
}
