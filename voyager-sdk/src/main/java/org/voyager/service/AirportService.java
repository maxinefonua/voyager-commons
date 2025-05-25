package org.voyager.service;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import java.util.List;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithPayload;
import static org.voyager.utils.ConstantsUtils.*;

public class AirportService {
    private final String servicePath;

    private static final Logger LOGGER = LoggerFactory.getLogger(AirportService.class);

    AirportService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getAirportsServicePath();
    }

    public Either<ServiceError,List<Airport>> getAirports() {
        return fetch(servicePath,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getAirports(@NonNull Airline airline) {
        String requestURL = servicePath.concat(String.format("?%s=%s",AIRLINE_PARAM_NAME,airline.name()));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getAirports(@NonNull AirportType airportType) {
        String requestURL = servicePath.concat(String.format("?%s=%s",TYPE_PARAM_NAME,airportType.name()));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getAirports(@NonNull AirportType airportType,
                                                          @NonNull Airline airline) {
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s",
                TYPE_PARAM_NAME,airportType.name(),AIRLINE_PARAM_NAME,airline.name()));
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,Airport> getAirport(@NonNull String iata) {
        String requestURL = servicePath.concat(String.format("/%s",iata));
        return fetch(requestURL,HttpMethod.GET,Airport.class);
    }

    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit) {
        String requestURL = servicePath.concat(String.format("?%s=%f" + "&%s=%f" + "&%s=%d",
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit)
        );
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit,
                                                                @NonNull AirportType airportType) {
        String requestURL = servicePath.concat(String.format("?%s=%f" + "&%s=%f" + "&%s=%d" + "&%s=%s",
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit,
                TYPE_PARAM_NAME,airportType.name())
        );
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit,
                                                                @NonNull Airline airline) {
        String requestURL = servicePath.concat(String.format("?%s=%f" + "&%s=%f" + "&%s=%d" + "&%s=%s",
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit,
                AIRLINE_PARAM_NAME,airline.name()));
       return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit,
                                                                @NonNull AirportType airportType,
                                                                @NonNull Airline airline) {
        String requestURL = servicePath.concat(String.format("?%s=%f" + "&%s=%f" + "&%s=%d" + "&%s=%s" + "&%s=%s",
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit,
                TYPE_PARAM_NAME,airportType.name(),
                AIRLINE_PARAM_NAME,airline.name())
        );
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,Airport> patchAirport(@NonNull String iata, @NonNull AirportPatch airportPatch) {
        String requestURL = servicePath.concat(String.format("/%s",iata));
        return fetchWithPayload(requestURL,HttpMethod.PATCH,Airport.class,airportPatch);
    }

}
