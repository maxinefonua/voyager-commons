package org.voyager.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.model.FlightQuery;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;

import java.util.List;

public interface FlightService {
    Either<ServiceError, List<Flight>> getFlights(FlightQuery flightQuery);
    Either<ServiceError, Flight> getFlight(@NonNull Integer id);
    Either<ServiceError, Flight> getFlight(@NonNull Integer routeId, @Valid @NotBlank String flightNumber);
    Either<ServiceError, Flight> createFlight(@NonNull @Valid FlightForm flightForm);
    Either<ServiceError, Flight> patchFlight(@NonNull Integer id, @NonNull @Valid FlightPatch flightPatch);
}
