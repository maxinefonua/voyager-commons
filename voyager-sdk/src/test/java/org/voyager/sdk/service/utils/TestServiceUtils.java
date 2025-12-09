package org.voyager.sdk.service.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceException;
import org.voyager.commons.model.geoname.response.GeoStatus;
import org.voyager.sdk.config.VoyagerConfig;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.http.VoyagerHttpFactoryTestImpl;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airport.Airport;
import org.voyager.commons.model.airport.AirportPatch;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.flight.Flight;
import org.voyager.commons.model.geoname.GeoCountry;
import org.voyager.commons.model.geoname.GeoFull;
import org.voyager.commons.model.geoname.GeoPlace;
import org.voyager.commons.model.geoname.GeoTimezone;
import org.voyager.commons.model.geoname.response.GeoResponse;
import org.voyager.commons.model.location.Location;
import org.voyager.commons.model.location.Source;
import org.voyager.commons.model.response.SearchResult;
import org.voyager.commons.model.result.LookupAttribution;
import org.voyager.commons.model.result.ResultSearch;
import org.voyager.commons.model.result.ResultSearchFull;
import org.voyager.commons.model.route.Route;
import org.voyager.sdk.utils.ServiceUtilsDefault;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class TestServiceUtils extends ServiceUtilsDefault {
    private static final Airport AIRPORT =  Airport.builder().iata("IATA").name("NAME").city("CITY")
            .subdivision("SUBDIVISON").countryCode("COUNTRY_CODE").latitude(1.0).longitude(-1.0)
            .type(AirportType.CIVIL).zoneId(ZoneId.of("America/Los_Angeles")).build();
    private static final Country COUNTRY =  Country.builder().code("TS").name("NAME").capitalCity("CITY").build();
    private static final Flight FLIGHT = Flight.builder().flightNumber("DL988").build();
    private static final Location LOCATION = Location.builder().name("test location").build();
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
            case "/flight?routeId=101&flightNumber=DL988&onDay=2025-11-13&zoneId=America/Los_Angeles":
                FLIGHT.setId(125);
                return Either.right((T)FLIGHT);
            case "/flight?routeId=101&flightNumber=DL988&departureZDT=2025-11-13T04:11:00.476797Z":
                FLIGHT.setFlightNumber("DL988");
                FLIGHT.setRouteId(101);
                return Either.right((T)FLIGHT);
            case "/admin/locations/2":
                LOCATION.setId(2);
                return Either.right((T) LOCATION);
            case "/admin/location?source=MANUAL&sourceId=test-source-id":
                LOCATION.setSource(Source.MANUAL);
                LOCATION.setSourceId("test-source-id");
                return Either.right((T) LOCATION);
            case "/routes/555":
                return Either.right((T)ROUTE);
            case "/route?origin=HNL&destination=HND":
                ROUTE.setOrigin("HNL");
                ROUTE.setDestination("HND");
                return Either.right((T) ROUTE);
            case "/admin/search/attribution":
                return Either.right((T) LookupAttribution.builder().build());
            case "/admin/fetch/test-source-id":
                return Either.right((T) ResultSearchFull.builder().build());
            case "/admin/airlines?airline=EMIRATES":
                return Either.right((T)Integer.valueOf("1"));
            case "/admin/geonames/timezone?latitude=4.0&longitude=4.0&radius=10&date=date&lang=lang":
            case "/admin/geonames/timezone?latitude=4.0&longitude=4.0":
                return Either.right((T)GeoTimezone.builder().build());
            case "/admin/geonames/fetch/100":
                return Either.right((T) GeoFull.builder().build());
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
            case "/iata":
            case "/iata?airline=JAPAN":
            case "/iata?type=HISTORICAL":
                return Either.right((T) List.of("IATA"));
            case "/nearby-airports?latitude=1.0&longitude=-1.0&limit=3&airline=AIRNZ&type=UNVERIFIED":
                Airport nearby = AIRPORT.toBuilder().type(AirportType.UNVERIFIED).build();
                return Either.right((T) List.of(nearby));
            case "/airlines":
            case "/airlines?iata=HEL&operator=OR":
                return Either.right((T)List.of(Airline.DELTA,Airline.JAPAN));
            case "/countries":
                return Either.right((T)List.of(COUNTRY));
            case "/flights":
            case "/flights?page=0&size=20&start=2025-11-14T16:17:34.035784-08:00[America/Los_Angeles]&end=2025-11-15T16:17:34.036843-08:00[America/Los_Angeles]&flightNumber=DL100":
                return Either.right((T)List.of(FLIGHT));
            case "/admin/locations?limit=20":
            case "/admin/locations":
                return Either.right((T)List.of(LOCATION));
            case "/routes?origin=SJC":
            case "/routes":
                return Either.right((T)List.of(Route.builder().build()));
            case "/admin/search?query=test":
                return Either.right((T) SearchResult.builder().results(List.of(ResultSearch.builder().build())));
            case "/countries?countryCode=OC":
                return Either.right((T) List.of(COUNTRY));
            case "/admin/geonames/search?q=qr&maxRows=100&startRow=0lang=lg&type=xml&operator=AND&charset=UTF8&fuzzy=1.0&name=nm&name_equals=ne&name_startsWith=nsw&country=TO&countryBias=TO&continentCode=OC&adminCode1=ac1&adminCode2=ac2&adminCode3=ac3&adminCode4=ac4&adminCode5=ac5&featureClass=P&featureCode=ADM2H&cities=cities1000&style=FULL&isNameRequired=true&tag=tg&east=1.000000&west=1.000000&north=1.000000&south=1.000000&searchlang=sl&orderby=population&inclBbox=true":
            case "/admin/geonames/search?q=query&maxRows=100&startRow=0lang=en&type=xml&operator=AND&charset=UTF8&fuzzy=1.0":
            case "/admin/geonames/nearby?latitude=10.0&longitude=10.0&radius=1":
            case "/admin/geonames/nearby?latitude=10.0&longitude=10.0":
                return Either.right((T) GeoResponse.builder().totalResultsCount(10)
                        .results(new ArrayList<>(List.of(GeoPlace.builder().build())))
                        .build());
            case "/admin/geonames/countries":
                return Either.right((T)GeoResponse.builder().totalResultsCount(1)
                        .results(new ArrayList<>(List.of(GeoCountry.builder().build())))
                        .build());
            case "/admin/geonames/search?q=query&maxRows=100&startRow=0lang=en&type=xml&operator=AND&charset=UTF8&fuzzy=1.0&featureClass=L":
                return Either.left(new ServiceError(HttpStatus.BAD_REQUEST,new ServiceException("forced error for testing")));
            case "/admin/geonames/nearby?latitude=-1.0&longitude=19.0":
                return Either.left(new ServiceError(HttpStatus.BAD_REQUEST,new ServiceException("forced error for testing")));
            case "/admin/geonames/nearby?latitude=-1.0&longitude=19.0&radius=20":
                return Either.right((T)GeoResponse.builder().geoStatus(GeoStatus.builder().value(19).message("testing too many requests").build()).build());
            case "/admin/geonames/nearby?latitude=-1.0&longitude=19.0&radius=25":
                return Either.right((T)GeoResponse.builder().geoStatus(GeoStatus.builder().value(10).message("testing other geonames error").build()).build());
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
            case "/admin/airports/IATA":
                Airport patched = AIRPORT.toBuilder().build();
                patched.setType((AirportType.valueOf(((AirportPatch)requestBody).getType())));
                return Either.right((T)patched);
            case "/admin/countries":
                return Either.right((T)COUNTRY);
            case "/admin/flights":
            case "/admin/flights/30":
                return Either.right((T)FLIGHT);
            case "/admin/locations":
            case "/admin/locations/2":
                LOCATION.setId(2);
                return Either.right((T) LOCATION);
            case "/admin/routes":
            case "/admin/routes/555":
                return Either.right((T) ROUTE);
            case "/admin/airports":
                return Either.right((T)AIRPORT);
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetchWithRequestBody: '%s' not yet implemented for requestURL: %s",
                    requestBody,requestURL));
        }
    }

    @Override
    public Either<ServiceError, Void> fetchNoResponseBody(String requestURL, HttpMethod httpMethod) {
        switch (requestURL) {
            case "/admin/locations/2":
                return Either.right(null);
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetchNoResponseBody not yet implemented for requestURL: %s",
                    requestURL));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> Either<ServiceError, T> fetchWithRequestBody(String requestURL, HttpMethod httpMethod,
                                                            TypeReference<T> typeReference, Object requestBody) {
        switch (requestURL) {
            case "/admin/airlines":
                return Either.right((T)List.of(AirlineAirport.builder().airline(Airline.JAPAN).isActive(true)
                        .iata("TEST").build()));
            default:
                throw new RuntimeException(String.format(
                        "ServiceUtilsTest fetchWithRequestBody not yet implemented for requestURL: %s",
                        requestURL));
        }
    }
}
