package org.voyager.model.datasync.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirportFlightFR {
    String name;
    String city;
    String icao;
    Long distance;
    @JsonProperty("flights")
    Map<String, PlannedFR> flightNumberToPlannedMap;
}
