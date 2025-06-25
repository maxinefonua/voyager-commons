package org.voyager.model.flight;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.voyager.model.Airline;
import org.voyager.model.validate.ValidEnum;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class FlightForm {
    @NotBlank
    String flightNumber;
    @NotNull
    Integer routeId;
    Long departureTimestamp;
    Long departureOffset;
    Long arrivalTimestamp;
    Long arrivalOffset;
    @NotNull
    Boolean isActive;
    @NotBlank
    @ValidEnum(enumClass = Airline.class)
    String airline;
}
