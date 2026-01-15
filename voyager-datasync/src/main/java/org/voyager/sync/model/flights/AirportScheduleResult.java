package org.voyager.sync.model.flights;

import org.voyager.commons.model.airline.Airline;
import java.util.List;
import java.util.Set;

public class AirportScheduleResult {
    public String airportCode1;
    public String airportCode2;
    public int flightsCreated;
    public int flightsSkipped;
    public int flightsPatched;
    public Set<Airline> airlineSet;

    public AirportScheduleResult(String airportCode1, String airportCode2, int flightsCreated, int flightsPatched,
                                 int flightsSkipped, Set<Airline> airlineSet) {
        this.airportCode1 = airportCode1;
        this.airportCode2 = airportCode2;
        this.flightsCreated = flightsCreated;
        this.flightsPatched = flightsPatched;
        this.flightsSkipped = flightsSkipped;
        this.airlineSet = airlineSet;
    }
}
