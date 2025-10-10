package org.voyager.service;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.model.AirportQuery;
import org.voyager.model.NearbyAirportQuery;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import java.util.List;

public interface AirportService {
    Either<ServiceError,List<Airport>> getAirports();
    Either<ServiceError,List<Airport>> getAirports(@NonNull AirportQuery airportQuery);
    Either<ServiceError,Airport> getAirport(@NonNull String iata);
    Either<ServiceError,Airport> patchAirport(@NonNull String iata, @NonNull AirportPatch airportPatch);
    Either<ServiceError,List<String>> getIATACodes(@NonNull List<AirportType> airportTypeList);
    Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull NearbyAirportQuery nearbyAirportQuery);
}