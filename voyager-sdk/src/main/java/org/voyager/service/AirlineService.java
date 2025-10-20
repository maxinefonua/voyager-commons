package org.voyager.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.model.airline.Airline;
import org.voyager.model.AirlineQuery;
import org.voyager.model.airline.AirlineAirport;
import org.voyager.model.airline.AirlineBatchUpsert;

import java.util.List;

public interface AirlineService {
    Either<ServiceError, List<Airline>> getAirlines();
    Either<ServiceError, List<Airline>> getAirlines(@NonNull AirlineQuery airlineQuery);
    Either<ServiceError, List<AirlineAirport>> batchUpsert(@NonNull AirlineBatchUpsert airlineBatchUpsert);
    Either<ServiceError, Integer> batchDeleteAirline(@NonNull Airline airline);
}
