package org.voyager.service;

import io.vavr.control.Either;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.model.airport.AirportType;
import org.voyager.model.airport.AirportCH;
import java.io.IOException;
import java.util.Map;

public class ChAviationService {
    private static final String baseURL = "https://www.ch-aviation.com";
    private static final String airportsPath = "/airports/";
    private static final String SSSID_NAME = "CHASESSID";
    private static final String SSSID_VALUE = "38d2dfdbb29532455f26cb3945e44606";
    private static final String GUEST_SESS_ID = "GUEST_SESSION_ID";
    private static final String GUEST_SESS_VALUE = "ab33a85d87dfd9ec26cddd6593fc244f";
    private static final Logger LOGGER = LoggerFactory.getLogger(ChAviationService.class);

    private enum Direction {
        S,
        N,
        E,
        W
    }

    public static Either<ServiceError,AirportCH> getAirportCH(String iata) {
        try {
            Document doc = Jsoup.connect(baseURL.concat(airportsPath).concat(iata)).timeout(0)
                    .cookies(Map.of(SSSID_NAME,SSSID_VALUE,GUEST_SESS_ID,GUEST_SESS_VALUE))
                    .get();
            String dataUrl  = doc.getElementsByAttributeValue("href","#overview").first().attr("data-url");
            String airportName = doc.select(".section__heading").text();
            return extractAirportDetails(dataUrl,iata,airportName);
        } catch (IOException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown while sending GET request '%s'",baseURL.concat(airportsPath).concat(iata)),
                    e));
        }
    }

    private static Either<ServiceError,AirportCH> extractAirportDetails(String dataUrl, String iata, String airportName) {
        try {
            Document doc = Jsoup.connect(baseURL.concat(dataUrl)).timeout(0)
                    .cookies(Map.of(SSSID_NAME,SSSID_VALUE,GUEST_SESS_ID,GUEST_SESS_VALUE))
                    .get();
            Elements elements = doc.select(".data-label");
            AirportCH airportCH = AirportCH.builder().iata(iata).name(airportName).build();
            elements.forEach(element -> {
                Element parent = element.parent();
                Element value = parent.selectFirst(".data-value");
                switch (element.text()) {
                    case "Airport type": {
                        String type = value.text();
                        if (type.contains("Civil")) airportCH.setType(AirportType.CIVIL);
                        else if (type.contains("Military")) airportCH.setType(AirportType.MILITARY);
                        else if (type.contains("Airport no longer in use")) airportCH.setType(AirportType.HISTORICAL);
                        else {
                            LOGGER.info(String.format(
                                    "ChService returned %s with aiport type '%s'. Setting to OTHER.",iata,type));
                            airportCH.setType(AirportType.OTHER);
                        }
                        break;
                    }
                    case "Country": {
                        String countryLink = value.selectFirst(".link").attribute("href").getValue();
                        String countryString = "/countries/";
                        String code = countryLink.substring(countryLink.indexOf(countryString)+countryString.length());
                        airportCH.setCountryCode(code);
                        break;
                    }
                    case "Coordinates": {
                        Elements values = parent.select(".data-value");
                        Double latitude = convertCoordinate(values.get(0).text());
                        Double longitude = convertCoordinate(values.get(1).text());
                        airportCH.setLatitude(latitude);
                        airportCH.setLongitude(longitude);
                        break;
                    }
                }
            });
            return Either.right(airportCH);
        } catch (IOException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown while building URI from '%s'",baseURL.concat(dataUrl)),
                    e));
        }
    }

    private static Double convertCoordinate(String text) {
        String[] tokens = text.split(" ");
        int degree = Integer.parseInt(tokens[0].replaceAll("[^0-9]", ""));
        int minute = Integer.parseInt(tokens[1].replaceAll("[^0-9]", ""));
        int second = Integer.parseInt(tokens[2].replaceAll("[^0-9]", ""));
        Direction direction = Direction.valueOf(tokens[3]);
        double minutes = minute/60.0;
        double seconds = second/3600.0;
        double val = degree + minutes + seconds;
        if (direction.equals(Direction.S) || direction.equals(Direction.W)) val *= -1.0;
        return val;
    }
}
