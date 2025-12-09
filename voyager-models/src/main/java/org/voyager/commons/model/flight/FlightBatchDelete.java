package org.voyager.commons.model.flight;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidBoolean;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidNonNullField;

@Builder @Data
@ValidNonNullField
@NoArgsConstructor @AllArgsConstructor
public class FlightBatchDelete {
    @ValidEnum(enumClass = Airline.class,
            allowNull = true)
    String airline;

    @ValidBoolean(caseSensitive = false,
            allowNull = true)
    String isActive;

    @Min(value = 3, message = "must be a valid integer with a minimum value of 3")
    String daysPast;
}
