package org.voyager.service;
import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import java.util.List;
import java.util.StringJoiner;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;
import static org.voyager.utils.ConstantsUtils.*;

public class AirportService {
    private final String servicePath;
    private final String nearbyPath;

    AirportService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getAirportsServicePath();
        this.nearbyPath = voyagerConfig.getNearbyPath();
    }

    public Either<ServiceError,List<Airport>> getAirports() {
        return fetch(servicePath,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getAirports(@NonNull Airline airline) {
        String requestURL = String.format("%s" + "?%s=%s",
                servicePath,AIRLINE_PARAM_NAME,airline.name());
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getAirports(@NonNull AirportType airportType) {
        String requestURL = String.format("%s" + "?%s=%s",
                servicePath,TYPE_PARAM_NAME,airportType.name());
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getAirports(@NonNull List<AirportType> airportTypeList) {
        StringJoiner stringJoiner = new StringJoiner(",");
        airportTypeList.forEach(airportType -> stringJoiner.add(airportType.name()));
        String requestURL = String.format("%s" + "?%s=%s",
                servicePath,TYPE_PARAM_NAME,stringJoiner);
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getAirports(@NonNull AirportType airportType, @NonNull Airline airline) {
        String requestURL = String.format("%s" + "?%s=%s" + "&%s=%s",
                servicePath, TYPE_PARAM_NAME,airportType.name(),AIRLINE_PARAM_NAME,airline.name());
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,Airport> getAirport(@NonNull String iata) {
        String requestURL = String.format("%s/%s",servicePath,iata);
        return fetch(requestURL,HttpMethod.GET,Airport.class);
    }

    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit) {
        String requestURL = String.format("%s" + "?%s=%f" + "&%s=%f" + "&%s=%d",
                nearbyPath,
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit
        );
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit,
                                                                @NonNull AirportType airportType) {
        String requestURL = String.format("%s" + "?%s=%f" + "&%s=%f" + "&%s=%d" + "&%s=%s",
                nearbyPath,
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit,
                TYPE_PARAM_NAME,airportType.name()
        );
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit,
                                                                @NonNull Airline airline) {
        String requestURL = String.format("%s" + "?%s=%f" + "&%s=%f" + "&%s=%d" + "&%s=%s",
                nearbyPath,
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit,
                AIRLINE_PARAM_NAME,airline.name()
        );
       return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit,
                                                                @NonNull List<Airline> airlineList) {
        StringJoiner stringJoiner = new StringJoiner(",");
        airlineList.forEach(airline -> stringJoiner.add(airline.name()));
        String requestURL = String.format("%s" + "?%s=%f" + "&%s=%f" + "&%s=%d" + "&%s=%s",
                nearbyPath,
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit,
                AIRLINE_PARAM_NAME,stringJoiner
        );
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }


    public Either<ServiceError,List<Airport>> getNearbyAirports(@NonNull Double longitude,
                                                                @NonNull Double latitude,
                                                                @NonNull Integer limit,
                                                                @NonNull AirportType airportType,
                                                                @NonNull Airline airline) {
        String requestURL = String.format("%s" + "?%s=%f" + "&%s=%f" + "&%s=%d" + "&%s=%s" + "&%s=%s",
                nearbyPath,
                LONGITUDE_PARAM_NAME,longitude,
                LATITUDE_PARAM_NAME,latitude,
                LIMIT_PARAM_NAME,limit,
                TYPE_PARAM_NAME,airportType.name(),
                AIRLINE_PARAM_NAME,airline.name()
        );
        return fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    public Either<ServiceError,Airport> patchAirport(@NonNull String iata, @NonNull AirportPatch airportPatch) {
        String requestURL = String.format("%s/%s",servicePath,iata);
        return fetchWithRequestBody(requestURL,HttpMethod.PATCH,Airport.class,airportPatch);
    }

}
