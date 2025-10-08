package org.voyager.service.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.http.VoyagerHttpFactoryTestImpl;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.model.country.Country;
import org.voyager.model.flight.Flight;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.utils.ServiceUtilsDefault;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.util.List;

public class TestServiceUtils extends ServiceUtilsDefault {
    private static final Airport AIRPORT =  Airport.builder().iata("IATA").name("NAME").city("CITY")
            .subdivision("SUBDIVISON").countryCode("COUNTRY_CODE").latitude(1.0).longitude(-1.0)
            .type(AirportType.CIVIL).zoneId(ZoneId.of("America/Los_Angeles")).build();
    private static final Country COUNTRY =  Country.builder().code("TS").name("NAME").capitalCity("CITY").build();
    private static final Flight FLIGHT = Flight.builder().flightNumber("DL988").build();
    private static final Location LOCATION = Location.builder().name("test location").build();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Either<ServiceError, T> fetch(String requestURL, HttpMethod httpMethod, Class<T> responseType) {
        switch (requestURL) {
            case "/airports/IATA":
                return Either.right((T)AIRPORT);
            case "/countries/TS":
                return Either.right((T)COUNTRY);
            case "/flights/125":
                FLIGHT.setId(125);
                return Either.right((T)FLIGHT);
            case "/flight?routeId=101&flightNumber=DL988":
                FLIGHT.setFlightNumber("DL988");
                FLIGHT.setRouteId(101);
                return Either.right((T)FLIGHT);
            case "/locations/2":
                LOCATION.setId(2);
                return Either.right((T) LOCATION);
            case "/location?source=MANUAL&sourceId=test-source-id":
                LOCATION.setSource(Source.MANUAL);
                LOCATION.setSourceId("test-source-id");
                return Either.right((T) LOCATION);
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetch class: '%s' not yet implemented for requestURL: %s",
                    responseType.getName(),requestURL));
        }
    }

    @Override
    protected HttpRequest getRequest(URI uri, HttpMethod httpMethod) {
        return VoyagerHttpFactoryTestImpl.request(uri,httpMethod);
    }

    @Override
    protected Either<ServiceError, HttpResponse<String>> sendRequest(HttpRequest request) {
        String uriString = request.uri().toString();
        if (request.bodyPublisher().isPresent() && !request.bodyPublisher().get().equals(HttpRequest.BodyPublishers.noBody())) {
            HttpRequest.BodyPublisher bodyPublisher = request.bodyPublisher().get();
            String body = request.bodyPublisher().get().toString();
            throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetch uri: '%s' not yet implemented with body: %s",uriString,body));
        }
        switch (uriString) {
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetch uri: '%s' not yet implemented",uriString));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Either<ServiceError, T> fetch(String requestURL, HttpMethod httpMethod, TypeReference<T> typeReference) {
        switch (requestURL) {
            case "/airports":
                return Either.right((T) List.of(AIRPORT));
            case "/airports?countryCode=TO&airline=DELTA&type=CIVIL":
                return Either.right((T) List.of(AIRPORT));
            case "/iata?type=HISTORICAL":
                return Either.right((T) List.of("IATA"));
            case "/nearby-airports?latitude=1.0&longitude=-1.0&limit=3&airline=AIRNZ&type=UNVERIFIED":
                Airport nearby = AIRPORT.toBuilder().type(AirportType.UNVERIFIED).build();
                return Either.right((T) List.of(nearby));
            case "/airport-airlines?iata=IATA":
                return Either.right((T)List.of(Airline.DELTA,Airline.JAPAN));
            case "/countries":
                return Either.right((T)List.of(COUNTRY));
            case "/flights":
                return Either.right((T)List.of(FLIGHT));
            case "/flights?flightNumber=DL100":
                FLIGHT.setFlightNumber("DL100");
                return Either.right((T)List.of(FLIGHT));
            case "/locations":
                return Either.right((T)List.of(LOCATION));
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetch typeReference: '%s' not yet implemented for requestURL: %s",
                    typeReference.getType().getTypeName(),requestURL));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Either<ServiceError, T> fetchWithRequestBody(String requestURL, HttpMethod httpMethod, Class<T> responseType, Object requestBody) {
        switch (requestURL) {
            case "/airports/IATA":
                if (httpMethod.equals(HttpMethod.PATCH)) {
                    Airport patched = AIRPORT.toBuilder().build();
                    patched.setType((AirportType.valueOf(((AirportPatch)requestBody).getType())));
                    return Either.right((T)patched);
                }
            case "/countries":
                if (httpMethod.equals(HttpMethod.POST)) {
                    Country country = Country.builder().code("MM").build();
                    return Either.right((T)country);
                }
            case "/flights":
                if (httpMethod.equals(HttpMethod.POST)) {
                    return Either.right((T)FLIGHT);
                }
            case "/flights/30":
                if (httpMethod.equals(HttpMethod.PATCH)) {
                    return Either.right((T)FLIGHT);
                }
            case "/locations":
                return Either.right((T)LOCATION);
            case "/locations/2":
                LOCATION.setId(2);
                return Either.right((T) LOCATION);
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetchWithRequestBody: '%s' not yet implemented for requestURL: %s",
                    requestBody,requestURL));
        }
    }

    @Override
    public Either<ServiceError, Void> fetchNoResponseBody(String requestURL, HttpMethod httpMethod) {
        switch (requestURL) {
            case "/locations/2":
                return Either.right(null);
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetchNoResponseBody not yet implemented for requestURL: %s",
                    requestURL));
        }
    }
}
