package org.voyager.sdk.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.flight.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

public interface FlightService {
    Either<ServiceError, List<Flight>> getFlights();
    Either<ServiceError, List<Flight>> getFlights(FlightQuery flightQuery);
    Either<ServiceError, Flight> getFlight(@NonNull Integer id);
    Either<ServiceError, Flight> getFlightOnDate(
            @NonNull Integer routeId,
            @NonNull String flightNumber,
            @NonNull LocalDate localDate,
            @NonNull ZoneId zoneId);
    Either<ServiceError, Flight> createFlight(@NonNull @Valid FlightForm flightForm);
    Either<ServiceError, Integer> batchDelete(@NonNull FlightBatchDelete flightBatchDelete);
    Either<ServiceError, FlightBatchUpsertResult> batchUpsert(@NonNull FlightBatchUpsert flightBatchUpsert);
}
