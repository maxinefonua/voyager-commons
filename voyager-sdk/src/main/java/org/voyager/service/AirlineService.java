package org.voyager.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.AirlineQuery;

import java.util.List;

public interface AirlineService {
    Either<ServiceError, List<Airline>> getAirportAirlines(@NonNull AirlineQuery airlineQuery);
}
