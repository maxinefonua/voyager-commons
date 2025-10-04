package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.Airline;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;

import static org.voyager.utils.ConstantsUtils.*;

public class FlightService {
    private static final String FLIGHTS_PATH = "/flights";
    private final ServiceUtils serviceUtils;

    FlightService() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    FlightService(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    public Either<ServiceError, List<Flight>> getFlights() {
        return serviceUtils.fetch(FLIGHTS_PATH, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, List<Flight>> getFlights(Airline airline,boolean isActive) {
        String requestURL = FLIGHTS_PATH.concat(String.format("?%s=%s" + "&%s=%s",
                AIRLINE_PARAM_NAME,airline.name(),IS_ACTIVE_PARAM_NAME,isActive));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, List<Flight>> getFlights(Integer routeId, boolean isActive) {
        String requestURL = FLIGHTS_PATH.concat(String.format("?%s=%d" + "&%s=%s",
                ROUTE_ID_PARAM_NAME,routeId,IS_ACTIVE_PARAM_NAME,isActive));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, List<Flight>> getFlights(Integer routeId,boolean isActive,Airline airline) {
        String requestURL = FLIGHTS_PATH.concat(String.format("?%s=%d" + "&%s=%s" + "&%s=%s",
                ROUTE_ID_PARAM_NAME,routeId,IS_ACTIVE_PARAM_NAME,isActive,AIRLINE_PARAM_NAME,airline.name()));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }


    public Either<ServiceError, List<Flight>> getFlights(Integer routeId,String flightNumber) {
        String requestURL = FLIGHTS_PATH.concat(String.format("?%s=%d" + "&%s=%s",
                ROUTE_ID_PARAM_NAME,routeId,FLIGHT_NUMBER_PARAM_NAME,flightNumber));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, Flight> getFlight(Integer id) {
        String requestURL = FLIGHTS_PATH.concat(String.format("/%d",id));
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Flight.class);
    }

    public Either<ServiceError, Flight> createFlight(FlightForm flightForm) {
        return serviceUtils.fetchWithRequestBody(FLIGHTS_PATH,HttpMethod.POST,Flight.class,flightForm);
    }

    public Either<ServiceError, Flight> patchFlight(Flight flight, FlightPatch flightPatch) {
        String requestURL = FLIGHTS_PATH.concat(String.format("/%s",flight.getId()));
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Flight.class,flightPatch);
    }

}
