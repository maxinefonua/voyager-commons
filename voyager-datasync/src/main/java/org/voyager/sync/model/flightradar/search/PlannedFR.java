package org.voyager.sync.model.flightradar.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class PlannedFR {
    AirlineFR airline;
    @JsonProperty("utc")
    Map<String, FlightTimeFR> dateToTimeMap;
}
