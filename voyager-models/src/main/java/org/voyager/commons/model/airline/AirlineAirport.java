package org.voyager.commons.model.airline;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.NonNull;

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
