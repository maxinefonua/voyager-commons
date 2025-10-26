package org.voyager.sync.model.flightradar.search;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.Map;

@Data
public class CountryFR {
    FlightCountFR number;
    @JsonProperty("airports")
    Map<String,AirportFlightFR> iataToFlightsMap;
}
