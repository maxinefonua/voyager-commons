package org.voyager.commons.model.flight;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidBoolean;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidPatch;

@Builder @Data
@ValidPatch
@NoArgsConstructor @AllArgsConstructor
public class FlightBatchDelete {
    @ValidEnum(enumClass = Airline.class,
            allowNull = true)
    String airline;

    @ValidBoolean(caseSensitive = false,
            allowNull = true)
    String isActive;
}
