package org.voyager.service.model;

import lombok.Getter;

import java.util.List;
import java.util.StringJoiner;

import static org.voyager.utils.ConstantsUtils.*;

public class AirlineQuery {
    private static final String AIRLINES_PATH = "/airport-airlines";
    @Getter
    private List<String> iataList;

    public static String resolveRequestURL(AirlineQuery airlineQuery) {
        if (airlineQuery == null) return AIRLINES_PATH;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(AIRLINES_PATH);
        urlBuilder.append("?");

        List<String> iataList = airlineQuery.getIataList();
        if (iataList != null && !iataList.isEmpty()) {
            StringJoiner iataJoiner = new StringJoiner(",");
            iataList.forEach(iataJoiner::add);
            urlBuilder.append(String.format("%s=%s",IATA_PARAM_NAME,iataJoiner));
        }

        return urlBuilder.toString();
    }
}
