package org.voyager.model.airline;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

@Builder(toBuilder = true) @Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class AirlineAirport {
    @NonNull
    Airline airline;

    @NonNull
    String iata;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Boolean isActive;
}
