package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.FlightQuery;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.service.FlightService;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;

public class FlightServiceImpl implements FlightService {
    private final ServiceUtils serviceUtils;

    FlightServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    FlightServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }


    @Override
    public Either<ServiceError, List<Flight>> getFlights(FlightQuery flightQuery) {
        String requestURL = FlightQuery.resolveRequestURL(flightQuery);
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<Flight>>(){});
    }

    @Override
    public Either<ServiceError, Flight> getFlight(@NonNull Integer id) {
        String requestURL = Constants.Voyager.Path.FLIGHTS.concat(String.format("/%d",id));
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Flight.class);
    }

    @Override
    public Either<ServiceError, Flight> getFlight(@NonNull Integer routeId,@Valid @NotBlank String flightNumber) {
        String requestURL = String.format("%s" + "?%s=%d" + "&%s=%s", Constants.Voyager.Path.FLIGHT,
                Constants.Voyager.ParameterNames.ROUTE_ID_PARAM_NAME,routeId,
                Constants.Voyager.ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber);
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Flight.class);
    }

    @Override
    public Either<ServiceError, Flight> createFlight(@NonNull @Valid FlightForm flightForm) {
        return serviceUtils.fetchWithRequestBody(Constants.Voyager.Path.FLIGHTS,HttpMethod.POST,Flight.class,flightForm);
    }

    @Override
    public Either<ServiceError, Flight> patchFlight(@NonNull Integer id,@NonNull @Valid FlightPatch flightPatch) {
        String requestURL = String.format("%s/%s",Constants.Voyager.Path.FLIGHTS,id);
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Flight.class,flightPatch);
    }
}
