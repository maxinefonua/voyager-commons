package org.voyager.sync.service;

import io.vavr.control.Either;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;

public interface AirportProcessor {
    Either<ServiceError, Airport> createAirport(AirportForm airportForm);
}
