package org.voyager.sync.reference;

import org.voyager.sync.model.flightradar.AirportFR;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class VoyagerReference {
    public final Set<String> civilAirportCodeSet;
    public final Set<String> allAirportCodeSet;
    public final Map<String, AirportFR> missingAirportMap;

    public VoyagerReference() {
        allAirportCodeSet = ConcurrentHashMap.newKeySet();
        civilAirportCodeSet = ConcurrentHashMap.newKeySet();
        missingAirportMap = new ConcurrentHashMap<>();
    }
}
