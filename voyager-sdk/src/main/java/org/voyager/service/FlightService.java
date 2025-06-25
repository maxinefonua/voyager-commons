package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.model.route.Route;
import org.voyager.model.route.RoutePatch;

import static org.voyager.utils.ConstantsUtils.FLIGHT_NUMBER_PARAM_NAME;
import static org.voyager.utils.ConstantsUtils.ROUTE_ID_PARAM_NAME;

import java.util.List;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;

public class FlightService {
    private final String servicePath;
    FlightService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getFlightsPath();
    }

    public Either<ServiceError, List<Flight>> getFlights() {
        return fetch(servicePath, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, List<Flight>> getFlights(Integer routeId,String flightNumber) {
        String requestURL = servicePath.concat(String.format("?%s=%d" + "&%s=%s",
                ROUTE_ID_PARAM_NAME,routeId,FLIGHT_NUMBER_PARAM_NAME,flightNumber));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    public Either<ServiceError, Flight> getFlight(Integer routeId) {
        String requestURL = servicePath.concat(String.format("/%d",routeId));
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
