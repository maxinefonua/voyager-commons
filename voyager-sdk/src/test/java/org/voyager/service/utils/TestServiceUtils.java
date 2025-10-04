package org.voyager.service.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.utils.ServiceUtils;

import java.time.ZoneId;
import java.util.List;

public class TestServiceUtils implements ServiceUtils {
    private static final Airport AIRPORT =  Airport.builder().iata("IATA").name("NAME").city("CITY")
            .subdivision("SUBDIVISON").countryCode("COUNTRY_CODE").latitude(1.0).longitude(-1.0)
            .type(AirportType.CIVIL).zoneId(ZoneId.of("America/Los_Angeles")).build();
    ObjectMapper om = new ObjectMapper();

    @Override
    @SuppressWarnings("unchecked")
    public <T> Either<ServiceError, T> fetch(String requestURL, HttpMethod httpMethod, Class<T> responseType) {
        switch (requestURL) {
            case "/airports/IATA":
                return Either.right((T)AIRPORT);
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetch class: '%s' not yet implemented for requestURL: %s",
                    responseType.getName(),requestURL));
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
            default:
                throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetchWithRequestBody: '%s' not yet implemented for requestURL: %s",
                    requestBody,requestURL));
        }
    }

    @Override
    public Either<ServiceError, Boolean> fetchNoResponseBody(String requestURL, HttpMethod httpMethod) {
        switch (requestURL) {
            default -> throw new RuntimeException(String.format(
                    "ServiceUtilsTest fetchNoResponseBody not yet implemented for requestURL: %s",
                    requestURL));
        }
    }
}
