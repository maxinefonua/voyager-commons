package org.voyager.model.flight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.voyager.model.validate.ValidPatch;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor @ValidPatch
public class FlightPatch {
    Long departureTimestamp;
    Long departureOffset;
    Long arrivalTimestamp;
    Long arrivalOffset;
    Boolean isActive;
}
