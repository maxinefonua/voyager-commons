package org.voyager.service.airports;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.AirportServiceError;
import org.voyager.error.HttpStatus;
import org.voyager.http.VoyagerHttpClient;
import org.voyager.http.VoyagerHttpFactory;
import org.voyager.model.Airport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AirportService {
    private final String servicePath;
    private final VoyagerHttpFactory voyagerHttpFactory;
    private final ObjectMapper om = new ObjectMapper();

    public AirportService(VoyagerConfig voyagerConfig, VoyagerHttpFactory voyagerHttpFactory) {
        this.servicePath = voyagerConfig.getAirportsServicePath();
        this.voyagerHttpFactory = voyagerHttpFactory;
    }

    Either<AirportServiceError,List<Airport>> getAirports() {
        try {
            URI uri = new URI(servicePath);
            HttpRequest request = voyagerHttpFactory.getRequest(uri);
            VoyagerHttpClient client = voyagerHttpFactory.getClient();
            return processAirportsResponse(client.send(request));
        } catch (URISyntaxException | IOException | InterruptedException e) {
            return Either.left(new AirportServiceError(HttpStatus.INTERNAL_SERVER_ERROR,e.getMessage(),e));
        }
    }

    private Either<AirportServiceError, List<Airport>> processAirportsResponse(HttpResponse<String> response) {
        if (response.statusCode() != 200) {
            if (StringUtils.isNotBlank(response.body())) {
                String message = om.readValue(response.body(),)
            }
            String message = om.readValue(response.body());
            return Either.left(new AirportServiceError(response.statusCode(),));
        }
        return om.readValue(response,Airport[].class);
    }

    Airport getAirport(String iataCode) {
        return null;
    }
}
