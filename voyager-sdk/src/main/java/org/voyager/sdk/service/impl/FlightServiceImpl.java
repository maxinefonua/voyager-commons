package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightBatchUpsertResult;
import org.voyager.commons.model.flight.FlightBatchUpsert;
import org.voyager.commons.model.flight.FlightQuery;
import org.voyager.commons.model.flight.FlightForm;
import org.voyager.commons.model.flight.FlightBatchDelete;
import org.voyager.commons.validate.ValidationUtils;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.service.FlightService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;
import java.time.LocalDate;
import java.time.ZoneId;
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
    public Either<ServiceError, Flight> getFlightOnDate(@NonNull Integer routeId,
                                                        @NonNull String flightNumber,
                                                        @NonNull LocalDate localDate,
                                                        @NonNull ZoneId zoneId) {
        String requestURL = String.format("%s" + "?%s=%d" + "&%s=%s" + "&%s=%s" + "&%s=%s",
                Path.FLIGHT,
                ParameterNames.ROUTE_ID_PARAM_NAME,routeId,
                ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber,
                ParameterNames.ON_DAY,localDate,
                ParameterNames.ZONE_ID,zoneId.getId());
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Flight.class);
    }

    @Override
    public Either<ServiceError, Flight> createFlight(@NonNull @Valid FlightForm flightForm) {
        return serviceUtils.fetchWithRequestBody(Path.Admin.FLIGHTS,HttpMethod.POST,Flight.class,flightForm);
    }

    @Override
    public Either<ServiceError, Integer> batchDelete(@NonNull FlightBatchDelete flightBatchDelete) {
        ValidationUtils.validateAndThrow(flightBatchDelete);
        String requestURL = Path.Admin.FLIGHTS;
        return serviceUtils.fetchWithRequestBody(requestURL,HttpMethod.DELETE,Integer.class,flightBatchDelete);
    }

    @Override
    public Either<ServiceError, FlightBatchUpsertResult> batchUpsert(@NonNull FlightBatchUpsert flightBatchUpsert) {
        ValidationUtils.validateAndThrow(flightBatchUpsert);
        String requestURL = Path.Admin.FLIGHTS;
        return serviceUtils.fetchWithRequestBody(
                requestURL,HttpMethod.POST,FlightBatchUpsertResult.class, flightBatchUpsert);
    }
}
