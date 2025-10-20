package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.airline.Airline;
import org.voyager.model.AirlineQuery;
import org.voyager.model.airline.AirlineAirport;
import org.voyager.model.airline.AirlineBatchUpsert;
import org.voyager.service.AirlineService;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;
import java.util.List;

public class AirlineServiceImpl implements AirlineService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineServiceImpl.class);
    private final ServiceUtils serviceUtils;

    AirlineServiceImpl(){
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    AirlineServiceImpl(ServiceUtils serviceUtils){
        this.serviceUtils = serviceUtils;
    }

    @Override
    public Either<ServiceError, List<Airline>> getAirlines() {
        String requestURL = Constants.Voyager.Path.AIRLINES;
        LOGGER.debug(String.format("attempting to GET %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airline>>(){});
    }

    @Override
    public Either<ServiceError, List<Airline>> getAirlines(@NonNull AirlineQuery airlineQuery) {
        LOGGER.debug(String.format("attempting to GET %s",airlineQuery.getRequestURL()));
        return serviceUtils.fetch(airlineQuery.getRequestURL(),HttpMethod.GET,new TypeReference<List<Airline>>(){});
    }

    @Override
    public Either<ServiceError, List<AirlineAirport>> batchUpsert(@NonNull AirlineBatchUpsert airlineBatchUpsert) {
        String requestURL = Constants.Voyager.Path.AIRLINES;
        LOGGER.debug(String.format("attempting to PATCH %s with body: %s",requestURL, airlineBatchUpsert));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.POST,
                new TypeReference<List<AirlineAirport>>(){}, airlineBatchUpsert);
    }

    @Override
    public Either<ServiceError, Integer> batchDeleteAirline(@NonNull Airline airline) {
        String requestURL = String.format("%s?%s=%s",Constants.Voyager.Path.AIRLINES,
                Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME,airline);
        LOGGER.debug(String.format("attempting to DELETE %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.DELETE,Integer.class);
    }
}
