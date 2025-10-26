package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.constants.Path;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.model.AirportQuery;
import org.voyager.sdk.model.IataQuery;
import org.voyager.sdk.model.NearbyAirportQuery;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportForm;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.sdk.service.AirportService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;
import java.util.List;

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
        String requestURL = Path.AIRPORTS;
        LOGGER.debug(String.format("attempting to GET airports from: %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    @Override
    public Either<ServiceError, List<Airport>> getAirports(@NonNull AirportQuery airportQuery) {
        LOGGER.debug(String.format("attempting to GET airports from: %s",airportQuery.getRequestURL()));
        return serviceUtils.fetch(airportQuery.getRequestURL(),HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }

    @Override
    public Either<ServiceError,Airport> getAirport(@NonNull String iata) {
        String requestURL = String.format("%s/%s",Path.AIRPORTS,iata);
        LOGGER.debug(String.format("attempting to GET airport from: %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,Airport.class);
    }

    @Override
    public Either<ServiceError,Airport> patchAirport(@NonNull String iata, @NonNull AirportPatch airportPatch) {
        String requestURL = String.format("%s/%s",Path.Admin.AIRPORTS,iata);
        LOGGER.debug(String.format("attempting to PATCH airport at: %s, with: '%s'",requestURL,airportPatch));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Airport.class,airportPatch);
    }

    @Override
    public Either<ServiceError, Airport> createAirport(@NonNull AirportForm airportForm) {
        String requestURL = Path.Admin.AIRPORTS;
        LOGGER.debug("attempting to POST at {} with airportForm {}",requestURL,airportForm);
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.POST,Airport.class,airportForm);
    }

    @Override
    public Either<ServiceError, List<String>> getIATACodes() {
        String requestURL = Path.IATA;
        LOGGER.debug(String.format("attempting to GET iata codes from: %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<String>>(){});
    }

    @Override
    public Either<ServiceError, List<String>> getIATACodes(@NonNull IataQuery iataQuery) {
        LOGGER.debug(String.format("attempting to GET iata codes from: %s",iataQuery.getRequestURL()));
        return serviceUtils.fetch(iataQuery.getRequestURL(),HttpMethod.GET,new TypeReference<List<String>>(){});
    }

    @Override
    public Either<ServiceError, List<Airport>> getNearbyAirports(@NonNull NearbyAirportQuery nearbyAirportQuery) {
        LOGGER.debug(String.format("attempting to GET nearby airports from: %s",nearbyAirportQuery.getRequestURL()));
        return serviceUtils.fetch(nearbyAirportQuery.getRequestURL(),HttpMethod.GET,new TypeReference<List<Airport>>(){});
    }
}
