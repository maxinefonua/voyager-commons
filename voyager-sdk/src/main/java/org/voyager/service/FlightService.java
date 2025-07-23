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

import java.util.List;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;
import static org.voyager.utils.ConstantsUtils.*;

public class FlightService {
    private final String servicePath;
    FlightService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getFlightsPath();
    }

    public Either<ServiceError, List<Flight>> getFlights() {
        return fetch(servicePath, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, List<Flight>> getFlights(Airline airline,boolean isActive) {
        String requestURL = servicePath.concat(String.format("?%s=%s" + "&%s=%s",
                AIRLINE_PARAM_NAME,airline.name(),IS_ACTIVE_PARAM_NAME,isActive));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, List<Flight>> getFlights(Integer routeId, boolean isActive) {
        String requestURL = servicePath.concat(String.format("?%s=%d" + "&%s=%s",
                ROUTE_ID_PARAM_NAME,routeId,IS_ACTIVE_PARAM_NAME,isActive));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, List<Flight>> getFlights(Integer routeId,boolean isActive,Airline airline) {
        String requestURL = servicePath.concat(String.format("?%s=%d" + "&%s=%s" + "&%s=%s",
                ROUTE_ID_PARAM_NAME,routeId,IS_ACTIVE_PARAM_NAME,isActive,AIRLINE_PARAM_NAME,airline.name()));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }


    public Either<ServiceError, List<Flight>> getFlights(Integer routeId,String flightNumber) {
        String requestURL = servicePath.concat(String.format("?%s=%d" + "&%s=%s",
                ROUTE_ID_PARAM_NAME,routeId,FLIGHT_NUMBER_PARAM_NAME,flightNumber));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, Flight> getFlight(Integer id) {
        String requestURL = servicePath.concat(String.format("/%d",id));
        return fetch(requestURL, HttpMethod.GET, Flight.class);
    }

    public Either<ServiceError, Flight> createFlight(FlightForm flightForm) {
        return fetchWithRequestBody(servicePath,HttpMethod.POST,Flight.class,flightForm);
    }

    public Either<ServiceError, Flight> patchFlight(Flight flight, FlightPatch flightPatch) {
        String requestURL = servicePath.concat(String.format("/%s",flight.getId()));
        return fetchWithRequestBody(requestURL,HttpMethod.PATCH,Flight.class,flightPatch);
    }

}
