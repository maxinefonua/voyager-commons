package org.voyager.sync.service;

import org.voyager.commons.model.airline.Airline;
import org.voyager.sync.model.flights.AirportScheduleFailure;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ResultProcessor {
    void process(List<AirportScheduleFailure> failureList);
}
