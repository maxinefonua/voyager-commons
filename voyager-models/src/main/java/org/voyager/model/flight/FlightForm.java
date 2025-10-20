package org.voyager.model.flight;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import org.voyager.model.airline.Airline;
import org.voyager.model.validate.annotations.ValidEnum;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class FlightForm {
    @NotBlank
    private String flightNumber;

    @NotNull
    private Integer routeId;

    private Long departureTimestamp;
    private Long departureOffset;
    private Long arrivalTimestamp;
    private Long arrivalOffset;

    @Builder.Default
    private Boolean isActive = false;

    @NotBlank
    @ValidEnum(enumClass = Airline.class)
    private String airline;
}
