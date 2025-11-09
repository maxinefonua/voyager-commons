package org.voyager.commons.model.flight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.commons.validate.annotations.ValidBoolean;
import org.voyager.commons.validate.annotations.ValidNonNullField;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor @ValidNonNullField
public class FlightPatch {
    private Long departureTimestamp;
    private Long departureOffset;
    private Long arrivalTimestamp;
    private Long arrivalOffset;
    @ValidBoolean(allowNull = true)
    private String isActive;
}
