package org.voyager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.model.AirportType;
import org.voyager.model.airport.AirportCH;
import java.io.IOException;

public class FetchAirportFromCh implements Runnable {
    private static final String baseURL = "https://www.ch-aviation.com";
    private static final String airportsPath = "/airports/";
    private static final ObjectMapper om = new ObjectMapper();
    private static final String SSSID_NAME = "CHASESSID";
    private static final String SSSID_VALUE = "a509a5e7c8ed9b49ea205d5da9dfe54a";
    private static final String GUEST_SESS_ID = "GUEST_SESSION_ID";
    private static final String GUEST_SESS_VALUE = "22e92641364d3d42cedbabb9f2db8367";
    private String iata;

    public FetchAirportFromCh(String iata) {
        this.iata = iata;
    }

    @Override
    public void run() {
        
    }

    private enum Direction {
        S,
        N,
        E,
        W
    }

    public static Either<ServiceError,AirportCH> getAirportCH(String iata) {
        try {
            Document doc = Jsoup.connect(baseURL.concat(airportsPath).concat(iata))
                    .cookie(SSSID_NAME,SSSID_VALUE)
                    .cookie(GUEST_SESS_ID,GUEST_SESS_VALUE)
                    .get();
            String dataUrl  = doc.getElementsByAttributeValue("href","#overview").first().attr("data-url");
            return extractAirportInfo(dataUrl);
        } catch (IOException e) {
            return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                    String.format("Exception thrown while sending GET request '%s'",baseURL.concat(airportsPath).concat(iata)),
                    e));
        }
    }

    private static Either<ServiceError,AirportCH> extractAirportInfo(String dataUrl) {
        try {
            Document doc = Jsoup.connect(baseURL.concat(dataUrl))
                    .cookie("CHASESSID", "a509a5e7c8ed9b49ea205d5da9dfe54a")
                    .cookie("GUEST_SESSION_ID", "22e92641364d3d42cedbabb9f2db8367")
                    .get();
            Elements elements = doc.select(".data-label");
            AirportCH airportCH = AirportCH.builder().build();
            elements.forEach(element -> {
                Element parent = element.parent();
                Element value = parent.selectFirst(".data-value");
                switch (element.text()) {
                    case "Airport type": {
                        String type = value.text();
                        if (type.contains("Civil")) airportCH.setType(AirportType.CIVIL);
                        else if (type.contains("Military")) airportCH.setType(AirportType.MILITARY);
                        else {
                            System.out.println(value);
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
