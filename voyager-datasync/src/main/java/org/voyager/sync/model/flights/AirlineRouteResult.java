package org.voyager.sync.model.flights;

import org.voyager.commons.model.airline.Airline;
import java.util.Map;
import java.util.Set;

public class AirlineRouteResult {
    public Airline airline;
    public Map<String, Set<String>> originToDestinationSet;

    public AirlineRouteResult(Airline airline, Map<String,Set<String>> originToDestinationSet) {
        this.airline = airline;
        this.originToDestinationSet = originToDestinationSet;
    }
}
