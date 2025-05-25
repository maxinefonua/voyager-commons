package org.voyager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.voyager.http.VoyagerHttpClient;
import org.voyager.http.VoyagerHttpFactory;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

class RouteServiceTest {

    private static RouteService routeService;

    @Mock
    VoyagerHttpFactory voyagerHttpFactory;
    @Mock
    VoyagerHttpClient voyagerHttpClient;
    @Mock
    HttpResponse<String> httpResponse;
    @Mock
    HttpRequest httpRequest;

    @BeforeAll
    static void init() throws JsonProcessingException {
//        ObjectMapper om = new ObjectMapper();
//        Route[] routeArray = om.readValue(JSON_BODY_AIRPORTS,Airport[].class);
//        airportList = List.of(airportArray);
//
//        airport = om.readValue(JSON_BODY_AIRPORT,Airport.class);
    }

    @BeforeEach
    void setUp() {
    }
}