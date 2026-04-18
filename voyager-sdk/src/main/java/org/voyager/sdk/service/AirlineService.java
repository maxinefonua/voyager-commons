package org.voyager.sdk.service;

import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineQuery;
import java.util.List;

public interface AirlineService {
    Either<ServiceError, List<Airline>> getAirlines();
    Either<ServiceError, List<Airline>> getAirlines(@NonNull AirlineQuery airlineQuery);
}
