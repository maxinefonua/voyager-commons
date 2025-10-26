package org.voyager.commons.model.flight;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.*;

import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.annotations.ValidBoolean;
import org.voyager.commons.validate.annotations.ValidEnum;
import org.voyager.commons.validate.annotations.ValidFlightNumber;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class FlightForm {
    @ValidFlightNumber
    private String flightNumber;

    @NotNull
    private Integer routeId;

    private Long departureTimestamp;
    private Long departureOffset;
    private Long arrivalTimestamp;
    private Long arrivalOffset;

    @ValidBoolean
    @Builder.Default
    private String isActive = "false";

    @NotBlank
    @ValidEnum(enumClass = Airline.class)
    private String airline;
}
