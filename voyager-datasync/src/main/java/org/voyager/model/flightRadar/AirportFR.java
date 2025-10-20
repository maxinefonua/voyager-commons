package org.voyager.model.flightRadar;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
public class AirportFR {
    String iata;
    String icao;
    String name;
    String city;
    Double lat;
    Double lon;
    String country;
}
