package org.voyager.sync.service;

import org.voyager.commons.model.airline.Airline;
import java.util.Map;
import java.util.Set;

public interface AirlineAirportProcessor {
    void process(Map<Airline, Set<String>> airlineSetMap);
}
