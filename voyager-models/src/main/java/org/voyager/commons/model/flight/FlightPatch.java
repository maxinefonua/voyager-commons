package org.voyager.commons.model.flight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.commons.validate.annotations.ValidBoolean;
import org.voyager.commons.validate.annotations.ValidPatch;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor @ValidPatch
public class FlightPatch {
    private Long departureTimestamp;
    private Long departureOffset;
    private Long arrivalTimestamp;
    private Long arrivalOffset;
    @ValidBoolean(allowNull = true)
    private String isActive;
}
