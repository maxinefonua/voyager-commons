package org.voyager.service.airport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.http.VoyagerHttpClient;
import org.voyager.http.VoyagerHttpFactory;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

import static org.voyager.service.ServiceUtils.responseBody;

public class AirportService {
    private final String servicePath;
    private final VoyagerHttpFactory voyagerHttpFactory;
    private final ObjectMapper om = new ObjectMapper();
    private final URI serviceURI;

    public AirportService(@NonNull VoyagerConfig voyagerConfig, @NonNull VoyagerHttpFactory voyagerHttpFactory) {
        this.servicePath = voyagerConfig.getAirportsServicePath();
        this.voyagerHttpFactory = voyagerHttpFactory;
        try {
            this.serviceURI = new URI(servicePath);
        } catch (URISyntaxException e) { // TODO: correct exception to throw here?
            throw new IllegalArgumentException(String.format("Exception thrown creating URI for service path '%s'",servicePath),e);
        }
    }

    public Either<ServiceError,List<Airport>> getAirports() {
        HttpRequest request = voyagerHttpFactory.request(serviceURI,HttpMethod.GET);
        VoyagerHttpClient client = voyagerHttpFactory.getClient();
        Either<ServiceError, HttpResponse<String>> responseEither = client.send(request);
        if (responseEither.isLeft()) return Either.left(responseEither.getLeft());
        Either<ServiceError, Airport[]> either = responseBody(responseEither.get(),Airport[].class,servicePath);
        return either.map(airports -> Arrays.stream(airports).toList());
    }

    public Either<ServiceError,Airport> patchAirport(String iata, AirportPatch airportPatch) {
        String requestURL = servicePath.concat(String.format("/%s",iata));
        try {
            URI uri = new URI(requestURL);
            String payload = om.writeValueAsString(airportPatch);
            HttpRequest request = voyagerHttpFactory.request(uri,HttpMethod.PATCH,payload);
            VoyagerHttpClient client = voyagerHttpFactory.getClient();
            Either<ServiceError, HttpResponse<String>> responseEither = client.send(request);
            if (responseEither.isLeft()) return Either.left(responseEither.getLeft());
            return responseBody(responseEither.get(),Airport.class,requestURL);
        } catch (URISyntaxException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown will getting airort at url '%s'",requestURL),
                    e));
        } catch (JsonProcessingException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown before patch request to '%s' while writing payload of '%s'",iata,airportPatch),
                    e));
        }
    }


    public Either<ServiceError,Airport> getAirport(String iata) {
        String requestURL = servicePath.concat(String.format("/%s",iata));
        try {
            URI uri = new URI(requestURL);
            HttpRequest request = voyagerHttpFactory.request(uri,HttpMethod.GET);
            VoyagerHttpClient client = voyagerHttpFactory.getClient();
            Either<ServiceError, HttpResponse<String>> responseEither = client.send(request);
            if (responseEither.isLeft()) return Either.left(responseEither.getLeft());
            return responseBody(responseEither.get(),Airport.class,requestURL);
        } catch (URISyntaxException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown will getting airort at url '%s'",requestURL),
                    e));
        }
    }


}
