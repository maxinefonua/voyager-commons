package org.voyager.service.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.http.VoyagerHttpFactoryTestImpl;
import org.voyager.model.airline.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.model.country.Country;
import org.voyager.model.flight.Flight;
import org.voyager.model.location.Location;
import org.voyager.model.location.Source;
import org.voyager.model.response.SearchResult;
import org.voyager.model.result.LookupAttribution;
import org.voyager.model.result.ResultSearch;
import org.voyager.model.result.ResultSearchFull;
import org.voyager.model.route.Route;
import org.voyager.model.route.PathResponse;
import org.voyager.model.route.RouteAirline;
import org.voyager.model.route.AirlinePath;
import org.voyager.model.route.RoutePath;
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
    private static final PathResponse<AirlinePath> PATH_RESPONSE = PathResponse.<AirlinePath>builder()
            .responseList(List.of(AirlinePath.builder().airline(Airline.DELTA).build())).build();
    private static final Route ROUTE = Route.builder().id(555).build();

    private final VoyagerConfig voyagerConfig;

    protected TestServiceUtils(VoyagerConfig voyagerConfig) {
        super(voyagerConfig);
        this.voyagerConfig = voyagerConfig;
    }


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
            case "/routes/555":
                return Either.right((T)ROUTE);
            case "/route?origin=HNL&destination=HND":
                ROUTE.setOrigin("HNL");
                ROUTE.setDestination("HND");
                return Either.right((T) ROUTE);
            case "/search/attribution":
                return Either.right((T) LookupAttribution.builder().build());
            case "/search/test-source-id":
                return Either.right((T) ResultSearchFull.builder().build());
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
            case "/airports?countryCode=TO&airline=DELTA&type=CIVIL":
                return Either.right((T) List.of(AIRPORT));
            case "/iata?type=HISTORICAL":
                return Either.right((T) List.of("IATA"));
            case "/nearby-airports?latitude=1.0&longitude=-1.0&limit=3&airline=AIRNZ&type=UNVERIFIED":
                Airport nearby = AIRPORT.toBuilder().type(AirportType.UNVERIFIED).build();
                return Either.right((T) List.of(nearby));
            case "/airlines?iata=HEL":
                return Either.right((T)List.of(Airline.DELTA,Airline.JAPAN));
            case "/countries":
                return Either.right((T)List.of(COUNTRY));
            case "/flights":
            case "/flights?flightNumber=DL100":
                return Either.right((T)List.of(FLIGHT));
            case "/locations?limit=20":
            case "/locations":
                return Either.right((T)List.of(LOCATION));
            case "/airline-path?origin=SJC&destination=SLC":
                return Either.right((T)PATH_RESPONSE);
            case "/route-path?origin=SJC&destination=SLC":
                return Either.right((T)List.of(RoutePath.builder().routeAirlineList(List.of(
                        RouteAirline.builder().airlines(List.of(Airline.DELTA,Airline.UNITED)).build())).build()));
            case "/routes?origin=SJC":
            case "/routes":
                return Either.right((T)List.of(Route.builder().build()));
            case "/search?q=test":
                return Either.right((T) SearchResult.builder().results(List.of(ResultSearch.builder().build())));
            case "/countries?countryCode=OC":
                return Either.right((T) List.of(COUNTRY));
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
                return Either.right((T)COUNTRY);
            case "/flights":
            case "/flights/30":
                return Either.right((T)FLIGHT);
            case "/locations":
            case "/locations/2":
                LOCATION.setId(2);
                return Either.right((T) LOCATION);
            case "/routes":
            case "/routes/555":
                return Either.right((T) ROUTE);
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
