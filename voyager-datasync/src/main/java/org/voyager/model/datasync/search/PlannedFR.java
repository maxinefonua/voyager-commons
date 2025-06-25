package org.voyager.model.datasync.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class PlannedFR {
    @JsonProperty("utc")
    Map<String, FlightTimeFR> dateToTimeMap;
}
