package org.voyager.model.flight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.validate.annotations.ValidPatch;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor @ValidPatch
public class FlightPatch {
    private Long departureTimestamp;
    private Long departureOffset;
    private Long arrivalTimestamp;
    private Long arrivalOffset;
    private Boolean isActive;
}
