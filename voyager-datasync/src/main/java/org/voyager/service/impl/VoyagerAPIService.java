package org.voyager.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.voyager.model.Airline;
import org.voyager.model.route.RouteForm;
import org.voyager.service.VoyagerAPI;
import java.net.http.HttpResponse;

import static org.voyager.utils.ConstantsUtils.*;

public class VoyagerAPIService extends VoyagerAPI {

    public VoyagerAPIService() {
        super();
    }

    @Override
    public HttpResponse<String> getAirportByIata(String iata) {
        String fullURL = getBaseUrl().concat(AIRPORTS_PATH).concat("/").concat(iata);
        return getResponse(fullURL);
    }

    @Override
    public HttpResponse<String> getRoute() {
        String fullURL = getBaseUrl().concat(ROUTES_PATH);
        return getResponse(fullURL);
    }

    @Override
    public HttpResponse<String> getRoute(String origin, String destination, Airline airline) {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(ROUTES_PATH);
        sb.append("?");
        sb.append(ORIGIN_PARAM_NAME);
        sb.append("=");
        sb.append(origin);
        sb.append("&");
        sb.append(DESTINATION_PARAM_NAME);
        sb.append("=");
        sb.append(destination);
        sb.append("&");
        sb.append(AIRLINE_PARAM_NAME);
        sb.append("=");
        sb.append(airline);
        return getResponse(sb.toString());
    }

    @Override
    public HttpResponse<String> addRoute(String jsonBody) {
        StringBuilder sb = new StringBuilder();
        sb.append(getBaseUrl());
        sb.append(ROUTES_PATH);
        return postResponse(sb.toString(),jsonBody);
    }
}
