package org.voyager.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.model.AirportType;
import org.voyager.service.VerifyType;
import org.voyager.utils.ConstantsUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.voyager.utils.ConstantsUtils.AUTH_TOKEN_HEADER_NAME;
import static org.voyager.utils.ConstantsUtils.VOYAGER_API_KEY;

public class VerifyTypeVoyagerAPI implements VerifyType {
    Set<String> all = new HashSet<>(),civil = new HashSet<>(),military = new HashSet<>(),historical = new HashSet<>(),other = new HashSet<>();
    private static String IATA_ENDPOINT = "http://localhost:3000/iata";
    private static final Logger LOGGER = LoggerFactory.getLogger(VerifyTypeVoyagerAPI.class);
    private static final HttpClient CLIENT = HttpClient.newBuilder().build();
    @Override
    public void run() {
        ConstantsUtils.validateEnvironVars(List.of(VOYAGER_API_KEY));
        loadCodesToProcess();
        filterProcessed();
    }

    @Override
    public void loadCodesToProcess() {
        all = fetchCodes();
        civil = fetchCodesWithType(AirportType.CIVIL);
        military = fetchCodesWithType(AirportType.MILITARY);
        historical = fetchCodesWithType(AirportType.HISTORICAL);
        other = fetchCodesWithType(AirportType.OTHER);
    }

    @Override
    public void filterProcessed() {
        LOGGER.info(String.format("Before filtering, %d IATA codes left to process",all.size()));
        civil.forEach(all::remove);
        military.forEach(all::remove);
        historical.forEach(all::remove);
        other.forEach(all::remove);
        LOGGER.info(String.format("After filtering, %d IATA codes left to process",all.size()));
    }

    @Override
    public void processRemaining() {

    }

    @Override
    public void saveProcessed() {

    }

    private Set<String> fetchCodes() {
        Set<String> res = fetchCodesAtEndpoint(IATA_ENDPOINT);
        LOGGER.info(String.format("Loaded %d IATA codes from endpoint: %s",res.size(),IATA_ENDPOINT));
        return res;
    }

    private Set<String> fetchCodesWithType(AirportType type) {
        String urlWithParams = IATA_ENDPOINT + "?type=" + type;
        Set<String> res = fetchCodesAtEndpoint(urlWithParams);
        LOGGER.info(String.format("Loaded %d IATA codes from endpoint: %s",res.size(),urlWithParams));
        return res;
    }

    private Set<String> fetchCodesAtEndpoint(String fullURL) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(fullURL))
                    .headers(AUTH_TOKEN_HEADER_NAME, System.getenv(VOYAGER_API_KEY))
                    .GET()
                    .build();
            HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            if (status != 200) {
                throw new RuntimeException(String.format("Non-200 status returned from endpoint [%s]\nDeltaStatus: %d\nResponse: %s",fullURL,status, response.body()));
            }
            return parseCodesFromJsonString(response.body());
        } catch (URISyntaxException | InterruptedException | IOException e) {
            throw new RuntimeException(String.format("Error connecting to Voyager endpoint [%s]\nError message = %s",fullURL,e.getMessage()),e);
        }
    }

    private Set<String> parseCodesFromJsonString(String jsonString) {
        String[] tokens = jsonString.split(",");
        return Arrays.stream(tokens).map(token -> token.chars().filter(Character::isLetter)
                .mapToObj(c -> String.valueOf((char) c))
                .collect(Collectors.joining())).collect(Collectors.toSet());
    }
}
