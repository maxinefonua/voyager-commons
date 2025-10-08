package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.Airline;
import org.voyager.model.AirlineQuery;
import org.voyager.service.AirlineService;
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
    public Either<ServiceError, List<Airline>> getAirportAirlines(@NonNull AirlineQuery airlineQuery) {
        String requestURL = AirlineQuery.resolveRequestURL(airlineQuery);
        LOGGER.info(String.format("attempting to GET airlines from: %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Airline>>(){});
    }
}
