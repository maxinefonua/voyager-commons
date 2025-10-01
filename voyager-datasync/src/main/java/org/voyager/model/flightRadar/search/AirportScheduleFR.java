package org.voyager.model.flightRadar.search;

import lombok.Data;

import java.util.Map;

@Data
public class AirportScheduleFR {
    Map<String,CountryFR> arrivals;
    Map<String,CountryFR> departures;
}
