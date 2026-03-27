package org.voyager.sync.service;

import io.vavr.control.Either;
import org.voyager.sync.model.flightradar.search.AirportScheduleFR;
import org.voyager.sync.model.flights.AirportScheduleFailure;
import org.voyager.sync.model.flights.AirportScheduleResult;

public interface AirportScheduleProcessor {
    Either<AirportScheduleFailure, AirportScheduleResult> process(
            AirportScheduleFR airportScheduleFR, String airportCode1, String airportCode2);
}
