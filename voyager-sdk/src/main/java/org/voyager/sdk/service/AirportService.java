package org.voyager.sdk.service;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.model.AirportQuery;
import org.voyager.sdk.model.IataQuery;
import org.voyager.sdk.model.NearbyAirportQuery;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportPatch;

import java.util.List;

public interface AirportService {
    Either<ServiceError,List<Airport>> getAirports();
    Either<ServiceError,List<Airport>> getAirports(@NonNull AirportQuery airportQuery);
    Either<ServiceError,Airport> getAirport(@NonNull String iata);
    Either<ServiceError,Airport> patchAirport(@NonNull String iata, @NonNull AirportPatch airportPatch);
    Either<ServiceError,Airport> createAirport(@NonNull AirportForm airportForm);
    Either<ServiceError,List<String>> getIATACodes();
    Either<ServiceError,List<String>> getIATACodes(@NonNull IataQuery iataQuery);
    Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull NearbyAirportQuery nearbyAirportQuery);
}