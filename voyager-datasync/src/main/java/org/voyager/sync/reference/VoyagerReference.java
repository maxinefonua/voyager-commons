package org.voyager.sync.reference;

import org.voyager.commons.model.airport.Airport;
import org.voyager.sync.model.flightradar.AirportFR;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VoyagerReference {
    public final Map<String,Airport> civilAirportMap;
    public final Set<String> allAirportCodeSet;
    public final Map<String, AirportFR> missingAirportMap;

    public VoyagerReference() {
        allAirportCodeSet = ConcurrentHashMap.newKeySet();
        civilAirportMap = new ConcurrentHashMap<>();
        missingAirportMap = new ConcurrentHashMap<>();
    }
}
