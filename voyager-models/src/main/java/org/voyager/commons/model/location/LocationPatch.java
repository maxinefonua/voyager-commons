package org.voyager.commons.model.location;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidAirportCode;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidPatch;
import java.util.List;

@Data @NoArgsConstructor
@Builder @ToString(includeFieldNames = false)
@AllArgsConstructor @ValidPatch
public class LocationPatch {
    List<@ValidAirportCode(message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS)
            String> airports;

    @ValidEnum(enumClass = Status.class,
    message = "must be an accepted Status value")
    String status;
}
