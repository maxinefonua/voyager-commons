package org.voyager.commons.model.airline;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidAirportCode;
import org.voyager.commons.validate.annotations.ValidEnum;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString(includeFieldNames = false)
public class AirlineBatchUpsert {
    @ValidEnum(enumClass = Airline.class)
    String airline;

    @NotEmpty
    List<@ValidAirportCode(caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS) String> iataList;

    @NotNull
    Boolean isActive;
}
