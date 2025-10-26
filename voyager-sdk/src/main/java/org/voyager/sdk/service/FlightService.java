package org.voyager.sdk.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.model.FlightQuery;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightForm;
import org.voyager.commons.model.flight.FlightPatch;
import java.util.List;

public interface FlightService {
    Either<ServiceError, List<Flight>> getFlights();
    Either<ServiceError, List<Flight>> getFlights(FlightQuery flightQuery);
    Either<ServiceError, Flight> getFlight(@NonNull Integer id);
    Either<ServiceError, Flight> getFlight(@NonNull Integer routeId, @Valid @NotBlank String flightNumber);
    Either<ServiceError, Flight> createFlight(@NonNull @Valid FlightForm flightForm);
    Either<ServiceError, Flight> patchFlight(@NonNull Integer id, @NonNull @Valid FlightPatch flightPatch);
    Either<ServiceError, Integer> batchDelete(@NonNull FlightBatchDelete flightBatchDelete);
}
