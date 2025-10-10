package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.AirportQuery;
import org.voyager.model.NearbyAirportQuery;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.service.AirportService;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;
import java.util.List;
import java.util.StringJoiner;

public class AirportServiceImpl implements AirportService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirportServiceImpl.class);
    private final ServiceUtils serviceUtils;

    AirportServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    AirportServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    @Override
    public Either<ServiceError, List<Airport>> getAirports() {
        String requestURL = Constants.Voyager.Path.AIRPORTS;
        LOGGER.info(String.format("attempting to GET airports from: %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    @Override
    public Either<ServiceError, List<Airport>> getAirports(@NonNull AirportQuery airportQuery) {
        LOGGER.info(String.format("attempting to GET airports from: %s",airportQuery.getRequestURL()));
        return serviceUtils.fetch(airportQuery.getRequestURL(),HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    @Override
    public Either<ServiceError,Airport> getAirport(@NonNull String iata) {
        String requestURL = String.format("%s/%s",Constants.Voyager.Path.AIRPORTS,iata);
        LOGGER.info(String.format("attempting to GET airport from: %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Airport.class);
    }

    @Override
    public Either<ServiceError,Airport> patchAirport(@NonNull String iata, @NonNull AirportPatch airportPatch) {
        String requestURL = String.format("%s/%s",Constants.Voyager.Path.AIRPORTS,iata);
        LOGGER.info(String.format("attempting to PATCH airport at: %s, with: '%s'",requestURL,airportPatch));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Airport.class,airportPatch);
    }

    @Override
    public Either<ServiceError,List<String>> getIATACodes(@NonNull List<AirportType> airportTypeList) {
        StringJoiner typeJoiner = new StringJoiner(",");
        airportTypeList.forEach(airportType -> typeJoiner.add(airportType.name()));
        String requestURL = String.format("%s?" + "%s=%s",Constants.Voyager.Path.IATA,
                Constants.Voyager.ParameterNames.TYPE_PARAM_NAME,typeJoiner);
        LOGGER.info(String.format("attempting to GET iata codes from: %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<String>>(){});
    }

    @Override
    public Either<ServiceError, List<Airport>> getNearbyAirports(@NonNull NearbyAirportQuery nearbyAirportQuery) {
        LOGGER.info(String.format("attempting to GET nearby airports from: %s",nearbyAirportQuery.getRequestURL()));
        return serviceUtils.fetch(nearbyAirportQuery.getRequestURL(),HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }
}
