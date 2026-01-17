package org.voyager.sdk.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.*;

import java.util.List;

public interface AirlineService {
    Either<ServiceError, List<Airline>> getAirlines();
    Either<ServiceError, List<Airline>> getAirlines(@NonNull AirlineQuery airlineQuery);
    Either<ServiceError, AirlineBatchUpsertResult> batchUpsert(@NonNull AirlineBatchUpsert airlineBatchUpsert);
    Either<ServiceError, Integer> batchDeleteAirline(@NonNull Airline airline);
}
