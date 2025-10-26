package org.voyager.commons.model.airline;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import lombok.ToString;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidAirportCodeCollection;
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

    @ValidAirportCodeCollection(allowEmptyCollection = false,
            allowNullCollection = false,caseSensitive = true,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_NONEMPTY)
    List<String> iataList;

    @NotNull
    Boolean isActive;
}
