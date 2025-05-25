package org.voyager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.http.VoyagerHttpClient;
import org.voyager.http.VoyagerHttpFactory;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class AirportServiceTest {
    private static final String JSON_BODY_AIRPORTS = "[{\"iata\":\"EUA\",\"name\":\"Kaufana Airport\",\"city\":\"Eua Island\",\"subdivision\":\"ʻEua\",\"countryCode\":\"TO\",\"latitude\":-21.378299713134766,\"longitude\":-174.95799255371094,\"type\":\"OTHER\"},{\"iata\":\"HPA\",\"name\":\"Lifuka Island Airport\",\"city\":\"Lifuka\",\"subdivision\":\"Ha‘apai\",\"countryCode\":\"TO\",\"latitude\":-19.777000427246094,\"longitude\":-174.34100341796875,\"type\":\"OTHER\"},{\"iata\":\"NFO\",\"name\":\"Mata'aho Airport\",\"city\":\"Angaha\",\"subdivision\":\"Vava‘u\",\"countryCode\":\"TO\",\"latitude\":-15.570799827575684,\"longitude\":-175.63299560546875,\"type\":\"OTHER\"},{\"iata\":\"NTT\",\"name\":\"Kuini Lavenia Airport\",\"city\":\"Niuatoputapu\",\"subdivision\":\"Niuas\",\"countryCode\":\"TO\",\"latitude\":-15.977339744567871,\"longitude\":-173.79103088378906,\"type\":\"OTHER\"},{\"iata\":\"TBU\",\"name\":\"Fua'amotu International Airport\",\"city\":\"Nuku'alofa\",\"subdivision\":\"Tongatapu\",\"countryCode\":\"TO\",\"latitude\":-21.241199493408203,\"longitude\":-175.14999389648438,\"type\":\"OTHER\"},{\"iata\":\"VAV\",\"name\":\"Vava'u International Airport\",\"city\":\"Vava'u Island\",\"subdivision\":\"Vava‘u\",\"countryCode\":\"TO\",\"latitude\":-18.58530044555664,\"longitude\":-173.96200561523438,\"type\":\"OTHER\"}]";
    private static final String JSON_BODY_AIRPORT = "{\"iata\":\"HEL\",\"name\":\"Helsinki Vantaa Airport\",\"city\":\"Helsinki\",\"subdivision\":\"Uusimaa\",\"countryCode\":\"FI\",\"latitude\":60.31719970703125,\"longitude\":24.963300704956055,\"type\":\"OTHER\"}";
    private static final String HOST = "test";
    private static final int PORT = 1000;
    private static final int MAX_THREADS = 50;
    private static final String AUTH_TOKEN = "test-token";
    private static final String SERVICE_ERROR_MESSAGE = "test message for service error";
    private static final VoyagerConfig voyagerConfig = new VoyagerConfig(VoyagerConfig.Protocol.HTTP,HOST,PORT,MAX_THREADS,AUTH_TOKEN);
    private static final ServiceError serviceError = new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
            new ServiceException(SERVICE_ERROR_MESSAGE));
    private static final ObjectMapper om = new ObjectMapper();

    private static Either<ServiceError,HttpResponse<String>> eitherRight;
    private static Either<ServiceError,HttpResponse<String>> eitherLeft;
    private static List<Airport> airportList;
    private static Airport airport;

    private static AirportService airportService;

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
        ObjectMapper om = new ObjectMapper();
        Airport[] airportArray = om.readValue(JSON_BODY_AIRPORTS,Airport[].class);
        airportList = List.of(airportArray);

        airport = om.readValue(JSON_BODY_AIRPORT,Airport.class);
    }



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        assertNotNull(voyagerHttpFactory);
        when(voyagerHttpFactory.request(any(),any())).thenReturn(httpRequest);
        assertNotNull(voyagerHttpFactory.request(any(),any()));
        when(voyagerHttpFactory.request(any(),any(),any())).thenReturn(httpRequest);
        assertNotNull(voyagerHttpFactory.request(any(),any(),any()));
        when(voyagerHttpFactory.getClient()).thenReturn(voyagerHttpClient);
        assertNotNull(voyagerHttpFactory.getClient());

        eitherRight = Either.right(httpResponse);
        assertNotNull(eitherRight.get());
        eitherLeft = Either.left(serviceError);
        assertNotNull(eitherLeft.getLeft());

        airportService = new AirportService(voyagerConfig);
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    @DisplayName("valid constructor args")
    void testValidConstructor() {
        assertNotNull(airportService);
    }

    @Test
    @DisplayName("invalid constructor args")
    void testInvalidConstructor() {
        assertThrows(NullPointerException.class,() -> new AirportService(null));
        assertThrows(NullPointerException.class,() -> new AirportService(null));
        assertThrows(NullPointerException.class,() -> new AirportService(voyagerConfig));

        VoyagerConfig invalidConfig = new VoyagerConfig(VoyagerConfig.Protocol.HTTP,"invalid host",PORT,MAX_THREADS,AUTH_TOKEN);
        assertThrows(IllegalArgumentException.class,() -> new AirportService(invalidConfig));
    }

    @Test
    @DisplayName("valid airport list")
    void getAirports() {
        when(voyagerHttpClient.send(httpRequest)).thenReturn(eitherRight);
        assertNotNull(voyagerHttpClient.send(httpRequest));
        when(httpResponse.statusCode()).thenReturn(200);
        assertEquals(200,httpResponse.statusCode());
        when(httpResponse.body()).thenReturn(JSON_BODY_AIRPORTS);

        Either<ServiceError, List<Airport>> either = airportService.getAirports();
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(airportList,either.get());
    }

    @Test
    @DisplayName("valid airport")
    void getAirport() {
        when(voyagerHttpClient.send(httpRequest)).thenReturn(eitherRight);
        assertNotNull(voyagerHttpClient.send(httpRequest));
        when(httpResponse.statusCode()).thenReturn(200);
        assertEquals(200,httpResponse.statusCode());
        when(httpResponse.body()).thenReturn(JSON_BODY_AIRPORT);

        Either<ServiceError, Airport> either = airportService.getAirport("TEST");
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(airport,either.get());
    }

    @Test
    @DisplayName("valid patch airport")
    void patchAirport() throws JsonProcessingException {
        when(voyagerHttpClient.send(httpRequest)).thenReturn(eitherRight);
        assertNotNull(voyagerHttpClient.send(httpRequest));
        when(httpResponse.statusCode()).thenReturn(200);
        assertEquals(200,httpResponse.statusCode());

        AirportPatch airportPatch = AirportPatch.builder().city("Changed City Name").build();
        Airport expected = airport.toBuilder().city(airportPatch.getCity()).build();
        String responseBody = om.writeValueAsString(expected);
        when(httpResponse.body()).thenReturn(responseBody);

        Either<ServiceError, Airport> either = airportService.patchAirport("TEST",airportPatch);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(expected,either.get());
    }
}