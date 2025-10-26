package org.voyager.sync.service;

import io.vavr.control.Either;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.sync.config.external.ChAviationConfig;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.sync.model.chaviation.AirportCH;
import java.io.IOException;

public class ChAviationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ChAviationService.class);
    private static final ChAviationConfig chAviationConfig = new ChAviationConfig();

    private enum Direction {
        S,
        N,
        E,
        W
    }

    public static Either<ServiceError,AirportCH> getAirportCH(String iata) {
        String requestURL = String.format("%s/%s", chAviationConfig.getAirportsPath(),iata);
        try {
            LOGGER.debug(String.format("fetching %s details from ChAviationService",iata));
            Document doc = Jsoup.connect(requestURL).timeout(0).get();
            String dataUrl  = doc.getElementsByAttributeValue("href","#overview").first().attr("data-url");
            String airportName = doc.select(".section__heading").text();
            return extractAirportDetails(dataUrl,iata,airportName);
        } catch (IOException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown while sending GET request '%s'",requestURL),
                    e));
        }
    }

    private static Either<ServiceError,AirportCH> extractAirportDetails(String dataUrl, String iata, String airportName) {

        String requestURL = chAviationConfig.getBaseURL().concat(dataUrl);
        try {
            Document doc = Jsoup.connect(requestURL).timeout(0).get();
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
                    String.format("Exception thrown while building URI from '%s'",requestURL),
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
