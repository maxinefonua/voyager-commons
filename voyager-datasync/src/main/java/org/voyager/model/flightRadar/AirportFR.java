package org.voyager.model.flightRadar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString(includeFieldNames = false)
public class AirportFR {
    String country;
    String iata;
    String icao;
    Double lat;
    Double lon;
    String name;
}
