package org.voyager.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.voyager.model.Airline;
import org.voyager.model.AirportType;
import org.voyager.model.delta.DeltaDisplay;
import org.voyager.model.delta.DeltaForm;
import org.voyager.model.delta.DeltaPatch;
import org.voyager.model.route.RouteDisplay;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.VoyagerAPI;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.voyager.utils.ConstantsUtils.*;

public class VoyagerAPIService extends VoyagerAPI {
    private static final ObjectMapper om = new ObjectMapper();

    private static final AtomicReference<Integer> non200Counter = new AtomicReference<>(0);
    private static final AtomicReference<Integer> jsonExceptionCounter = new AtomicReference<>(0);

    public VoyagerAPIService(int maxConcurrentRequests) {
        super(maxConcurrentRequests);
    }

    @Override
    public HttpResponse<String> getAirportByIata(String iata) throws InterruptedException {
        String fullURL = getBaseUrl().concat(AIRPORTS_PATH).concat("/").concat(iata);
        return getResponse(fullURL);
    }

    @Override
    public HttpResponse<String> getRoutes() throws InterruptedException {
        String fullURL = getBaseUrl().concat(ROUTES_PATH);
        return getResponse(fullURL);
    }

    @Override
    public DeltaDisplay getDelta(String iata){
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(DELTA_PATH);
        sb.append("/");
        sb.append(iata);
        try {
            return processGetResponse(getResponse(sb.toString()), DeltaDisplay.class);
        } catch (InterruptedException e) {
            LOGGER.debug(String.format("InterruptedException thrown when getting delta with iata %s. Returning null. Error: %s",iata,e.getMessage()),e);
            return null;
        }
    }

    @Override
    public List<DeltaDisplay> getAllDelta() throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(DELTA_PATH);
        return Arrays.stream(processGetResponse(getResponse(sb.toString()),DeltaDisplay[].class)).toList();
    }

    @Override
    public DeltaDisplay addDelta(DeltaForm deltaForm) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(DELTA_PATH);
        try {
            String jsonBody = om.writeValueAsString(deltaForm);
            return processPostResponse(postResponse(sb.toString(),jsonBody),DeltaDisplay.class);
        } catch (JsonProcessingException e) {
            String message = String.format("Error writing json from deltaForm: %s. Message: %s", deltaForm, e.getMessage());
            LOGGER.error(message,e);
            jsonExceptionCounter.getAndSet(jsonExceptionCounter.get() + 1);
            throw new InterruptedException(message);
        }
    }

    @Override
    public List<String> getAllCivilIata() throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(IATA_PATH);
        sb.append("?");
        sb.append(TYPE_PARAM_NAME);
        sb.append("=");
        sb.append(AirportType.CIVIL.name());
        return Arrays.stream(processGetResponse(getResponse(sb.toString()),String[].class)).toList();
    }

    @Override
    public Boolean hasActiveRoutesFrom(String origin,Airline airline) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(ROUTES_PATH);
        sb.append("?");
        sb.append(ORIGIN_PARAM_NAME);
        sb.append("=");
        sb.append(origin);
        sb.append("&");
        sb.append(AIRLINE_PARAM_NAME);
        sb.append("=");
        sb.append(airline.name());
        sb.append("&");
        sb.append(IS_ACTIVE_PARAM_NAME);
        sb.append("=");
        sb.append(true);
        return processGetResponse(getResponse(sb.toString()),RouteDisplay[].class).length > 0;
    }

    private <T> T processGetResponse(HttpResponse<String> response, Class<T> valueType) throws InterruptedException {
        if (response.statusCode() == 200) {
            String routesResponseBody = response.body();
            try {
                return om.readValue(routesResponseBody,valueType);
            } catch (JsonProcessingException e) {
                String message = String.format("Error reading json from routesResponseBody: %s. Message: %s", routesResponseBody, e.getMessage());
                LOGGER.error(message,e);
                jsonExceptionCounter.getAndSet(jsonExceptionCounter.get() + 1);
                throw new InterruptedException(message);
            }
        } else if (response.statusCode() == 404) {
            LOGGER.debug(String.format("404 returned from response: %s. Returning null.",response));
            return null;
        } else {
            String message = String.format("Non-200 returned from URI: %s",response.uri().toString());
            LOGGER.error(message);
            non200Counter.getAndSet(non200Counter.get() + 1);
            throw new InterruptedException(message);
        }
    }

    @Override
    public RouteDisplay getRoute(RouteForm routeForm) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(ROUTES_PATH);
        sb.append("?");
        sb.append(ORIGIN_PARAM_NAME);
        sb.append("=");
        sb.append(routeForm.getOrigin());
        sb.append("&");
        sb.append(DESTINATION_PARAM_NAME);
        sb.append("=");
        sb.append(routeForm.getDestination());
        sb.append("&");
        sb.append(AIRLINE_PARAM_NAME);
        sb.append("=");
        sb.append(routeForm.getAirline());
        RouteDisplay[] results = processGetResponse(getResponse(sb.toString()),RouteDisplay[].class);
        if (results.length == 0) return null;
        else if (results.length == 1) {
            LOGGER.debug(String.format("existing route: %s", results[0]));
            return results[0];
        } else {
            String message = String.format("more than one route returned for routeForm: %s", routeForm);
            LOGGER.error(message);
            throw new InterruptedException(message);
        }
    }

    @Override
    public RouteDisplay addRoute(RouteForm routeForm) throws InterruptedException {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(ROUTES_PATH);
        try {
            String jsonBody = om.writeValueAsString(routeForm);
            return processPostResponse(postResponse(sb.toString(),jsonBody),RouteDisplay.class);
        } catch (JsonProcessingException e) {
            String message = String.format("JsonProcessingException thrown when writing json from routeForm: %s. Message: %s", routeForm, e.getMessage());
            LOGGER.error(message,e);
            jsonExceptionCounter.getAndSet(jsonExceptionCounter.get() + 1);
            throw new InterruptedException(message);
        }
    }

    private <T> T processPostResponse(HttpResponse<String> postResponse, Class<T> valueType) throws InterruptedException {
        if (postResponse.statusCode() == 200) {
            String jsonResponseBody = postResponse.body();
            try {
                return om.readValue(jsonResponseBody, valueType);
            } catch (JsonProcessingException e) {
                String message = String.format("JsonProcessingException thrown when reading jsonResponseBody: %s. Message: %s", jsonResponseBody, e.getMessage());
                LOGGER.error(message,e);
                jsonExceptionCounter.getAndSet(jsonExceptionCounter.get() + 1);
                throw new InterruptedException(message);
            }
        } else {
            String message = String.format("Non-200 returned from URI: %s",postResponse.uri().toString());
            LOGGER.error(message);
            non200Counter.getAndSet(non200Counter.get() + 1);
            throw new InterruptedException(message);
        }
    }

    @Override
    public RouteDisplay patchRoute(RouteDisplay routeDisplay, RoutePatch routePatch) throws InterruptedException {
        try {
            String patchJson = om.writeValueAsString(routePatch);
            StringBuilder sb = new StringBuilder();
            sb.append(getBaseUrl());
            sb.append(ROUTES_PATH);
            sb.append("/");
            sb.append(routeDisplay.getId());
            return processPatchResponse(patchResponse(sb.toString(),patchJson),RouteDisplay.class);
        } catch (JsonProcessingException e) {
            String message = String.format("JsonProcessingException thrown when writing json from routePatch: %s. Message: %s", routePatch, e.getMessage());
            LOGGER.error(message,e);
            jsonExceptionCounter.getAndSet(jsonExceptionCounter.get() + 1);
            throw new InterruptedException(message);
        }
    }

    @Override
    public void printProcessingErrorCounts() {
        LOGGER.info(String.format("non200Counter: %d", non200Counter.get()));
        LOGGER.info(String.format("jsonExceptionCounter: %d", jsonExceptionCounter.get()));
    }

    @Override
    public DeltaDisplay patchDelta(String iata, DeltaPatch deltaPatch) throws InterruptedException {
        try {
            String patchJson = om.writeValueAsString(deltaPatch);
            StringBuilder sb = new StringBuilder();
            sb.append(getBaseUrl());
            sb.append(DELTA_PATH);
            sb.append("/");
            sb.append(iata);
            return processPatchResponse(patchResponse(sb.toString(),patchJson),DeltaDisplay.class);
        } catch (JsonProcessingException e) {
            String message = String.format("JsonProcessingException thrown when writing json from deltaPatch: %s. Message: %s", deltaPatch, e.getMessage());
            LOGGER.error(message,e);
            jsonExceptionCounter.getAndSet(jsonExceptionCounter.get() + 1);
            throw new InterruptedException(message);
        }
    }

    private <T> T processPatchResponse(HttpResponse<String> patchResponse, Class<T> valueType) throws InterruptedException {
        if (patchResponse.statusCode() == 200) {
            String jsonResponseBody = patchResponse.body();
            try {
                return om.readValue(jsonResponseBody, valueType);
            } catch (JsonProcessingException e) {
                String message = String.format("JsonProcessingException thrown when reading jsonResponseBody: %s. Message: %s", jsonResponseBody, e.getMessage());
                LOGGER.error(message,e);
                jsonExceptionCounter.getAndSet(jsonExceptionCounter.get() + 1);
                throw new InterruptedException(message);
            }
        } else {
            String message = String.format("Non-200 returned from URI: %s",patchResponse.uri().toString());
            LOGGER.error(message);
            non200Counter.getAndSet(non200Counter.get() + 1);
            throw new InterruptedException(message);
        }
    }
}
