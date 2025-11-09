package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.validate.ValidationUtils;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.commons.model.flight.FlightQuery;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.model.flight.FlightForm;
import org.voyager.commons.model.flight.FlightPatch;
import org.voyager.sdk.service.FlightService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;
import java.util.List;

public class FlightServiceImpl implements FlightService {
    private final ServiceUtils serviceUtils;

    FlightServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    @SuppressWarnings("unused")
    FlightServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }


    @Override
    public Either<ServiceError, List<Flight>> getFlights() {
        String requestURL = Path.FLIGHTS;
        return serviceUtils.fetch(requestURL,HttpMethod.GET, new TypeReference<>() {
        });
    }

    @Override
    public Either<ServiceError, List<Flight>> getFlights(FlightQuery flightQuery) {
        ValidationUtils.validateAndThrow(flightQuery);
        return serviceUtils.fetch(flightQuery.getRequestURL(),HttpMethod.GET, new TypeReference<>() {
        });
    }

    @Override
    public Either<ServiceError, Flight> getFlight(@NonNull Integer id) {
        String requestURL = Path.FLIGHTS.concat(String.format("/%d",id));
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Flight.class);
    }

    @Override
    public Either<ServiceError, Flight> getFlight(@NonNull Integer routeId,@Valid @NotBlank String flightNumber) {
        String requestURL = String.format("%s" + "?%s=%d" + "&%s=%s", Path.FLIGHT,
                ParameterNames.ROUTE_ID_PARAM_NAME,routeId,
                ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber);
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Flight.class);
    }

    @Override
    public Either<ServiceError, Flight> createFlight(@NonNull @Valid FlightForm flightForm) {
        return serviceUtils.fetchWithRequestBody(Path.Admin.FLIGHTS,HttpMethod.POST,Flight.class,flightForm);
    }

    @Override
    public Either<ServiceError, Flight> patchFlight(@NonNull Integer id,@NonNull @Valid FlightPatch flightPatch) {
        String requestURL = String.format("%s/%s",Path.Admin.FLIGHTS,id);
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.PATCH,Flight.class,flightPatch);
    }

    @Override
    public Either<ServiceError, Integer> batchDelete(@NonNull FlightBatchDelete flightBatchDelete) {
        String requestURL = Path.Admin.FLIGHTS;
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.DELETE,Integer.class,flightBatchDelete);
    }
}
