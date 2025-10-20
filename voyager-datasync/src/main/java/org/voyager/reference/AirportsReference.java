package org.voyager.reference;

import lombok.Getter;
import org.voyager.model.airline.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.flightRadar.AirportFR;

import java.util.*;

@Getter
public class AirportsReference {
    private final Map<String, Airport> airportCodeMap;
    private final Map<String, Airport> civilAirportCodeMap;
    private final List<String> allAirportCodes;
    private final List<String> civilAirportCodes;

    // failures
    private final List<Airline> failedFetchAirlineList;
    private final Set<String> missingAirportFromRoute;
    private final Map<String,AirportFR> missingAirportMap;
    private final Map<String,AirportFR> skippedNonCivilMap;

    public AirportsReference() {
        airportCodeMap = new HashMap<>();
        civilAirportCodeMap = new HashMap<>();
        civilAirportCodes = new ArrayList<>();
        allAirportCodes = new ArrayList<>();

        missingAirportMap = new HashMap<>();
        skippedNonCivilMap = new HashMap<>();
        failedFetchAirlineList = new ArrayList<>();
        missingAirportFromRoute = new HashSet<>();
    }
}
