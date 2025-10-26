package org.voyager.sdk.utils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.sdk.config.Protocol;
import org.voyager.sdk.config.VoyagerConfig;
import org.voyager.commons.constants.Headers;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.error.ServiceException;
import org.voyager.sdk.error.ServiceHttpException;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.http.MockHttpResponse;
import org.voyager.sdk.http.VoyagerHttpFactory;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.flight.FlightForm;
import org.voyager.commons.model.location.Location;
import org.voyager.commons.model.route.Route;
import org.voyager.sdk.service.impl.VoyagerServiceRegistry;

import java.net.ConnectException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ServiceUtilsDefaultTest {
    private static final VoyagerConfig VOYAGER_CONFIG = new VoyagerConfig(Protocol.HTTP,"test.org","test-token",true);
    private static final String BASE_URL = "http://test.org";
    private static final String COUNTRY_URL = "/countries/to";
    private static final String COUNTRY_JSON = "{\"code\":\"TO\",\"name\":\"Tonga\",\"population\":103197,\"capitalCity\":\"Nuku'alofa\",\"areaInSqKm\":748.0,\"continent\":\"Oceania\",\"bounds\":[-176.21263122558594,-22.34571647644043,-173.7366485595703,-15.566146850585938]}";

    private static final String ROUTES_URL = "/routes?origin=sjc";
    private static final String ROUTES_JSON = "[{\"id\":1625,\"origin\":\"SJC\",\"destination\":\"SLC\",\"distanceKm\":938.89691},{\"id\":1624,\"origin\":\"SJC\",\"destination\":\"SEA\",\"distanceKm\":1122.04908},{\"id\":1870,\"origin\":\"SJC\",\"destination\":\"VNY\",\"distanceKm\":null},{\"id\":1620,\"origin\":\"SJC\",\"destination\":\"ATL\",\"distanceKm\":3397.62655},{\"id\":3965,\"origin\":\"SJC\",\"destination\":\"AUS\",\"distanceKm\":2371.38808},{\"id\":3967,\"origin\":\"SJC\",\"destination\":\"BOI\",\"distanceKm\":841.24283},{\"id\":3968,\"origin\":\"SJC\",\"destination\":\"BUR\",\"distanceKm\":476.73006},{\"id\":3969,\"origin\":\"SJC\",\"destination\":\"BWI\",\"distanceKm\":3915.03826},{\"id\":3970,\"origin\":\"SJC\",\"destination\":\"DAL\",\"distanceKm\":2328.54601},{\"id\":3973,\"origin\":\"SJC\",\"destination\":\"GEG\",\"distanceKm\":1195.43782},{\"id\":3974,\"origin\":\"SJC\",\"destination\":\"HNL\",\"distanceKm\":3885.73967},{\"id\":3975,\"origin\":\"SJC\",\"destination\":\"HOU\",\"distanceKm\":2605.23248},{\"id\":3978,\"origin\":\"SJC\",\"destination\":\"OGG\",\"distanceKm\":3787.96324},{\"id\":3979,\"origin\":\"SJC\",\"destination\":\"ONT\",\"distanceKm\":536.41936},{\"id\":3982,\"origin\":\"SJC\",\"destination\":\"RNO\",\"distanceKm\":303.0199},{\"id\":3985,\"origin\":\"SJC\",\"destination\":\"STL\",\"distanceKm\":2753.76053},{\"id\":3976,\"origin\":\"SJC\",\"destination\":\"LGB\",\"distanceKm\":521.44494},{\"id\":3984,\"origin\":\"SJC\",\"destination\":\"SNA\",\"distanceKm\":550.46802},{\"id\":1621,\"origin\":\"SJC\",\"destination\":\"LAS\",\"distanceKm\":620.43284},{\"id\":1622,\"origin\":\"SJC\",\"destination\":\"LAX\",\"distanceKm\":495.73784},{\"id\":3966,\"origin\":\"SJC\",\"destination\":\"BNA\",\"distanceKm\":3125.64174},{\"id\":3981,\"origin\":\"SJC\",\"destination\":\"PHX\",\"distanceKm\":998.67141},{\"id\":3977,\"origin\":\"SJC\",\"destination\":\"MDW\",\"distanceKm\":2950.27514},{\"id\":3980,\"origin\":\"SJC\",\"destination\":\"PDX\",\"distanceKm\":916.34129},{\"id\":3983,\"origin\":\"SJC\",\"destination\":\"SAN\",\"distanceKm\":671.41958},{\"id\":6622,\"origin\":\"SJC\",\"destination\":\"DTW\",\"distanceKm\":3311.05782},{\"id\":1623,\"origin\":\"SJC\",\"destination\":\"MSP\",\"distanceKm\":2530.00194},{\"id\":5951,\"origin\":\"SJC\",\"destination\":\"IAH\",\"distanceKm\":11975.97978},{\"id\":6373,\"origin\":\"SJC\",\"destination\":\"ORD\",\"distanceKm\":11043.00766},{\"id\":4912,\"origin\":\"SJC\",\"destination\":\"KOA\",\"distanceKm\":12503.9031},{\"id\":4913,\"origin\":\"SJC\",\"destination\":\"LIH\",\"distanceKm\":12411.77383},{\"id\":6859,\"origin\":\"SJC\",\"destination\":\"MSY\",\"distanceKm\":11975.40307},{\"id\":4914,\"origin\":\"SJC\",\"destination\":\"PVR\",\"distanceKm\":12466.74556},{\"id\":4915,\"origin\":\"SJC\",\"destination\":\"SJD\",\"distanceKm\":12358.08199},{\"id\":4911,\"origin\":\"SJC\",\"destination\":\"GDL\",\"distanceKm\":2595.4683},{\"id\":3972,\"origin\":\"SJC\",\"destination\":\"EUG\",\"distanceKm\":759.55914},{\"id\":3971,\"origin\":\"SJC\",\"destination\":\"DEN\",\"distanceKm\":1522.26539},{\"id\":8651,\"origin\":\"SJC\",\"destination\":\"DFW\",\"distanceKm\":2310.1967},{\"id\":9261,\"origin\":\"SJC\",\"destination\":\"BJX\",\"distanceKm\":2681.74735},{\"id\":9262,\"origin\":\"SJC\",\"destination\":\"MLM\",\"distanceKm\":2807.84262},{\"id\":9263,\"origin\":\"SJC\",\"destination\":\"ZCL\",\"distanceKm\":2442.69649},{\"id\":9350,\"origin\":\"SJC\",\"destination\":\"NRT\",\"distanceKm\":8276.09181}]";

    private static final String FLIGHT_URL = "/flights";
    private static final String FLIGHT_JSON = "{\"id\":52983,\"flightNumber\":\"HA465\",\"routeId\":4533,\"zonedDateTimeDeparture\":\"2025-07-04T02:30:00Z\",\"zonedDateTimeArrival\":\"2025-07-04T08:10:00Z\",\"isActive\":true,\"airline\":\"HAWAIIAN\"}";

    private static final String LOCATION_URL = "/locations/123";

    private static final String SERVICE_ERROR_URL = "/error";
    private static final String HEALTH_JSON = "{\"status\":\"UP\"}";

    private static final String SERVICE_ERROR_NO_BODY_URL = "/error/nobody";

    private static final String EXPOSED_SERVICE_ERROR_URL = "/error/exposed";
    private static final String EXPOSED_SERVICE_ERROR = "Error: service error is surfaced";

    private static final String SERVICE_EXCEPTION_URL = "/error/test";
    private static final String SERVICE_EXCEPTION_JSON = "{\"timestamp\":\"2025-10-10T18:59:16.959+00:00\",\"status\":404,\"error\":\"Not Found\",\"message\":\"No static resource test.\",\"path\":\"/test\"}";

    class ServiceUtilsTestClass extends ServiceUtilsDefault {
        protected ServiceUtilsTestClass(VoyagerConfig voyagerConfig) {
            super(voyagerConfig);
        }

        @Override
        protected HttpRequest getRequest(URI uri, HttpMethod httpMethod) {
            return HttpRequest.newBuilder(uri).build();
        }

        @Override
        protected HttpRequest getRequestWithBody(URI uri, HttpMethod httpMethod, String jsonPayload) {
            return HttpRequest.newBuilder(uri).build();
        }

        @BeforeEach
        void setup() {
            VoyagerServiceRegistry.getInstance().reset();
        }

        @Override
        protected Either<ServiceError, HttpResponse<String>> sendRequest(HttpRequest request) {
            String url = request.uri().toString().replaceFirst(BASE_URL,"");
            switch (url) {
                case "/admin/actuator/health":
                    return Either.right(new MockHttpResponse(HEALTH_JSON,200));
                case COUNTRY_URL:
                    return Either.right(new MockHttpResponse(COUNTRY_JSON,200));
                case ROUTES_URL:
                    return Either.right(new MockHttpResponse(ROUTES_JSON,200));
                case FLIGHT_URL:
                    return Either.right(new MockHttpResponse(FLIGHT_JSON,200));
                case LOCATION_URL:
                    return Either.right(new MockHttpResponse(null,204));
                case SERVICE_EXCEPTION_URL:
                    return Either.right(new MockHttpResponse(SERVICE_EXCEPTION_JSON,404));
                case EXPOSED_SERVICE_ERROR_URL:
                    return Either.right(new MockHttpResponse(EXPOSED_SERVICE_ERROR,500));
                case SERVICE_ERROR_NO_BODY_URL:
                    return Either.right(new MockHttpResponse(null,500));
            }
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR, new ServiceException()));
        }
    }

    ServiceUtilsTestClass serviceUtilsTestClass = new ServiceUtilsTestClass(VOYAGER_CONFIG);

    @Test
    void testVerifyHealth() {
        // with testMode true
        assertDoesNotThrow(()-> serviceUtilsTestClass.verifyHealth());

        // with testMode false
        VOYAGER_CONFIG.setTestMode(false);
        ServiceUtilsTestClass testClass = new ServiceUtilsTestClass(VOYAGER_CONFIG);
        assertDoesNotThrow(testClass::verifyHealth);
    }

    @Test
    void fetch() {
        Either<ServiceError,Country> either = serviceUtilsTestClass.fetch(COUNTRY_URL,HttpMethod.GET,Country.class);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals("Tonga",either.get().getName());

        either = serviceUtilsTestClass.fetch(SERVICE_ERROR_URL,HttpMethod.GET,Country.class);
        assertNotNull(either);
        assertTrue(either.isLeft());
        assertInstanceOf(ServiceException.class,either.getLeft().getException());

        either = serviceUtilsTestClass.fetch(SERVICE_EXCEPTION_URL,HttpMethod.GET,Country.class);
        assertNotNull(either);
        assertTrue(either.isLeft());
        assertInstanceOf(ServiceHttpException.class,either.getLeft().getException());
    }

    @Test
    void testFetch() {
        Either<ServiceError,List<Route>> either = serviceUtilsTestClass.fetch(ROUTES_URL,HttpMethod.GET,new TypeReference<List<Route>>(){});
        assertNotNull(either);
        assertTrue(either.isRight());
        assertFalse(either.get().isEmpty());
        assertEquals("SJC",either.get().get(0).getOrigin());
        assertEquals("SLC",either.get().get(0).getDestination());

        either = serviceUtilsTestClass.fetch(SERVICE_ERROR_URL,HttpMethod.GET,new TypeReference<List<Route>>(){});
        assertNotNull(either);
        assertTrue(either.isLeft());
        assertInstanceOf(ServiceException.class,either.getLeft().getException());

        either = serviceUtilsTestClass.fetch(SERVICE_EXCEPTION_URL,HttpMethod.GET,new TypeReference<List<Route>>(){});
        assertNotNull(either);
        assertTrue(either.isLeft());
    }

    @Test
    void fetchWithRequestBody() {
        FlightForm flightForm = FlightForm.builder().airline(Airline.HAWAIIAN.name()).build();
        Either<ServiceError, Flight> either = serviceUtilsTestClass
                .fetchWithRequestBody(FLIGHT_URL,HttpMethod.POST,Flight.class,flightForm);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(Airline.HAWAIIAN,either.get().getAirline());

        either = serviceUtilsTestClass.fetchWithRequestBody(SERVICE_ERROR_URL,HttpMethod.POST,Flight.class,flightForm);
        assertNotNull(either);
        assertTrue(either.isLeft());

        either = serviceUtilsTestClass.fetchWithRequestBody(SERVICE_EXCEPTION_URL,HttpMethod.POST,Flight.class,flightForm);
        assertNotNull(either);
        assertTrue(either.isLeft());
    }

    @Test
    void fetchNoResponseBody() {
        Either<ServiceError,Void> either = serviceUtilsTestClass.fetchNoResponseBody(LOCATION_URL,HttpMethod.POST);
        assertNotNull(either);
        assertTrue(either.isRight());

        either = serviceUtilsTestClass.fetchNoResponseBody(SERVICE_EXCEPTION_URL,HttpMethod.POST);
        assertNotNull(either);
        assertTrue(either.isLeft());
    }

    @Test
    void testBadURL () {
        assertThrows(IllegalArgumentException.class,()-> serviceUtilsTestClass
                .fetch("malformatted:url",HttpMethod.GET,String.class));
        assertThrows(IllegalArgumentException.class,()-> serviceUtilsTestClass
                .fetchWithRequestBody("malformatted:url",HttpMethod.GET,String.class,null));

        Either<ServiceError, String> either = serviceUtilsTestClass
                .fetch("http://malformatted url",HttpMethod.GET,String.class);
        assertTrue(either.isLeft());
        assertInstanceOf(URISyntaxException.class,either.getLeft().getException());

        either = serviceUtilsTestClass
                .fetchWithRequestBody("http://malformatted url",HttpMethod.GET,String.class,null);
        assertTrue(either.isLeft());
        assertInstanceOf(URISyntaxException.class,either.getLeft().getException());
    }

    @Test
    void testErrorStatusReturned() {
        Either<ServiceError, Country> either = serviceUtilsTestClass.fetch(SERVICE_EXCEPTION_URL,HttpMethod.GET,Country.class);
        assertNotNull(either);
        assertTrue(either.isLeft());
        assertInstanceOf(ServiceHttpException.class,either.getLeft().getException());
    }

    @Test
    void testServiceErrorExposed() {
        Either<ServiceError, Location> either = serviceUtilsTestClass
                .fetch(EXPOSED_SERVICE_ERROR_URL,HttpMethod.GET, Location.class);
        assertNotNull(either);
        assertTrue(either.isLeft());
        assertInstanceOf(JsonParseException.class,either.getLeft().getException());
    }

    @Test
    void testErrorNoResponseBody() {
        Either<ServiceError, Location> either = serviceUtilsTestClass
                .fetch(SERVICE_ERROR_NO_BODY_URL,HttpMethod.GET, Location.class);
        assertNotNull(either);
        assertTrue(either.isLeft());
        assertInstanceOf(ServiceException.class,either.getLeft().getException());
    }

    @Test
    void testCircularMappingWriteToValue() {
        class Node {
            public String name;
            public Node next;
        }

        Node node1 = new Node();
        Node node2 = new Node();
        node1.name = "First";
        node2.name = "Second";
        node1.next = node2;
        node2.next = node1;

        Either<ServiceError, Flight> either = serviceUtilsTestClass
                .fetchWithRequestBody(FLIGHT_URL,HttpMethod.POST,Flight.class,node1);
        assertTrue(either.isLeft());
        assertInstanceOf(JsonMappingException.class,either.getLeft().getException());
    }

    @Test
    void testIncorrectMappingClass() {
        Either<ServiceError,Country> either = serviceUtilsTestClass.fetch(FLIGHT_URL,HttpMethod.GET,Country.class);
        assertTrue(either.isLeft());
        assertInstanceOf(JsonProcessingException.class,either.getLeft().getException());

        Either<ServiceError,List<Route>> either2 = serviceUtilsTestClass
                .fetch(FLIGHT_URL, HttpMethod.GET, new TypeReference<List<Route>>() {});
        assertTrue(either2.isLeft());
        assertInstanceOf(JsonProcessingException.class,either2.getLeft().getException());


        either = serviceUtilsTestClass
                .fetchWithRequestBody(FLIGHT_URL,HttpMethod.GET,Country.class,null);
        assertTrue(either.isLeft());
        assertInstanceOf(JsonProcessingException.class,either.getLeft().getException());
    }

    @Test
    void testProtectedMethods() throws URISyntaxException {
        VOYAGER_CONFIG.setTestMode(false);
        ServiceUtilsDefault serviceUtilsDefault = new ServiceUtilsDefault(VOYAGER_CONFIG);
        String authToken = "test-token";
        VoyagerHttpFactory.reset();
        VoyagerHttpFactory.initialize(authToken);
        String fullURL = BASE_URL.concat(LOCATION_URL);
        Either<ServiceError, HttpResponse<String>> either = serviceUtilsDefault
                .sendRequest(HttpRequest.newBuilder().uri(new URI(fullURL)).build());
        assertTrue(either.isLeft());
        assertInstanceOf(ConnectException.class,either.getLeft().getException());

        HttpRequest httpRequest = serviceUtilsDefault.getRequest(new URI(fullURL),HttpMethod.GET);
        assertNotNull(httpRequest);
        assertEquals(HttpMethod.GET.name(),httpRequest.method());
        assertTrue(httpRequest.headers().firstValue(Headers.AUTH_TOKEN_HEADER_NAME).isPresent());
        assertEquals(authToken,httpRequest.headers().firstValue(Headers.AUTH_TOKEN_HEADER_NAME).get());

        String jsonPayload = "[{\"id\":1625,\"origin\":\"SJC\",\"destination\":\"SLC\",\"distanceKm\":938.89691},{\"id\":1624,\"origin\":\"SJC\",\"destination\":\"SEA\",\"distanceKm\":1122.04908},{\"id\":1870,\"origin\":\"SJC\",\"destination\":\"VNY\",\"distanceKm\":null},{\"id\":1620,\"origin\":\"SJC\",\"destination\":\"ATL\",\"distanceKm\":3397.62655},{\"id\":3965,\"origin\":\"SJC\",\"destination\":\"AUS\",\"distanceKm\":2371.38808},{\"id\":3967,\"origin\":\"SJC\",\"destination\":\"BOI\",\"distanceKm\":841.24283},{\"id\":3968,\"origin\":\"SJC\",\"destination\":\"BUR\",\"distanceKm\":476.73006},{\"id\":3969,\"origin\":\"SJC\",\"destination\":\"BWI\",\"distanceKm\":3915.03826},{\"id\":3970,\"origin\":\"SJC\",\"destination\":\"DAL\",\"distanceKm\":2328.54601},{\"id\":3973,\"origin\":\"SJC\",\"destination\":\"GEG\",\"distanceKm\":1195.43782},{\"id\":3974,\"origin\":\"SJC\",\"destination\":\"HNL\",\"distanceKm\":3885.73967},{\"id\":3975,\"origin\":\"SJC\",\"destination\":\"HOU\",\"distanceKm\":2605.23248},{\"id\":3978,\"origin\":\"SJC\",\"destination\":\"OGG\",\"distanceKm\":3787.96324},{\"id\":3979,\"origin\":\"SJC\",\"destination\":\"ONT\",\"distanceKm\":536.41936},{\"id\":3982,\"origin\":\"SJC\",\"destination\":\"RNO\",\"distanceKm\":303.0199},{\"id\":3985,\"origin\":\"SJC\",\"destination\":\"STL\",\"distanceKm\":2753.76053},{\"id\":3976,\"origin\":\"SJC\",\"destination\":\"LGB\",\"distanceKm\":521.44494},{\"id\":3984,\"origin\":\"SJC\",\"destination\":\"SNA\",\"distanceKm\":550.46802},{\"id\":1621,\"origin\":\"SJC\",\"destination\":\"LAS\",\"distanceKm\":620.43284},{\"id\":1622,\"origin\":\"SJC\",\"destination\":\"LAX\",\"distanceKm\":495.73784},{\"id\":3966,\"origin\":\"SJC\",\"destination\":\"BNA\",\"distanceKm\":3125.64174},{\"id\":3981,\"origin\":\"SJC\",\"destination\":\"PHX\",\"distanceKm\":998.67141},{\"id\":3977,\"origin\":\"SJC\",\"destination\":\"MDW\",\"distanceKm\":2950.27514},{\"id\":3980,\"origin\":\"SJC\",\"destination\":\"PDX\",\"distanceKm\":916.34129},{\"id\":3983,\"origin\":\"SJC\",\"destination\":\"SAN\",\"distanceKm\":671.41958},{\"id\":6622,\"origin\":\"SJC\",\"destination\":\"DTW\",\"distanceKm\":3311.05782},{\"id\":1623,\"origin\":\"SJC\",\"destination\":\"MSP\",\"distanceKm\":2530.00194},{\"id\":5951,\"origin\":\"SJC\",\"destination\":\"IAH\",\"distanceKm\":11975.97978},{\"id\":6373,\"origin\":\"SJC\",\"destination\":\"ORD\",\"distanceKm\":11043.00766},{\"id\":4912,\"origin\":\"SJC\",\"destination\":\"KOA\",\"distanceKm\":12503.9031},{\"id\":4913,\"origin\":\"SJC\",\"destination\":\"LIH\",\"distanceKm\":12411.77383},{\"id\":6859,\"origin\":\"SJC\",\"destination\":\"MSY\",\"distanceKm\":11975.40307},{\"id\":4914,\"origin\":\"SJC\",\"destination\":\"PVR\",\"distanceKm\":12466.74556},{\"id\":4915,\"origin\":\"SJC\",\"destination\":\"SJD\",\"distanceKm\":12358.08199},{\"id\":4911,\"origin\":\"SJC\",\"destination\":\"GDL\",\"distanceKm\":2595.4683},{\"id\":3972,\"origin\":\"SJC\",\"destination\":\"EUG\",\"distanceKm\":759.55914},{\"id\":3971,\"origin\":\"SJC\",\"destination\":\"DEN\",\"distanceKm\":1522.26539},{\"id\":8651,\"origin\":\"SJC\",\"destination\":\"DFW\",\"distanceKm\":2310.1967},{\"id\":9261,\"origin\":\"SJC\",\"destination\":\"BJX\",\"distanceKm\":2681.74735},{\"id\":9262,\"origin\":\"SJC\",\"destination\":\"MLM\",\"distanceKm\":2807.84262},{\"id\":9263,\"origin\":\"SJC\",\"destination\":\"ZCL\",\"distanceKm\":2442.69649},{\"id\":9350,\"origin\":\"SJC\",\"destination\":\"NRT\",\"distanceKm\":8276.09181}]";

        httpRequest = serviceUtilsDefault
                .getRequestWithBody(new URI(fullURL),HttpMethod.POST,jsonPayload);
        assertNotNull(httpRequest);
        assertEquals(HttpMethod.POST.name(),httpRequest.method());
        assertTrue(httpRequest.bodyPublisher().isPresent());
        assertEquals(jsonPayload.getBytes().length,httpRequest.bodyPublisher().get().contentLength());

        assertThrows(RuntimeException.class, serviceUtilsDefault::verifyHealth);

        VoyagerHttpFactory.reset();
    }
}