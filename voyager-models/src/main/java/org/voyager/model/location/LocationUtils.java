package org.voyager.model.location;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LocationUtils {

    protected static boolean hasAirport(String[] airports, String iata) {
        if (airports == null) return false;
        return Arrays.asList(airports).contains(iata.toUpperCase());
    }

    protected static String[] addAirport(String[] airports, String iata) {
        if (hasAirport(airports,iata)) return airports;
        else if (airports == null) airports = new String[]{};
        Set<String> added = new HashSet<>(Arrays.stream(airports).filter(StringUtils::isNotBlank).toList());
        added.add(iata.toUpperCase());
        airports = added.toArray(String[]::new);
        return airports;
    }

    protected static String[] removeAirport(String[] airports, String iata) {
        if (hasAirport(airports,iata)) {
            Set<String> added = new HashSet<>(Arrays.stream(airports).filter(StringUtils::isNotBlank).toList());
            added.remove(iata.toUpperCase());
            airports = added.toArray(String[]::new);
        }
        return airports;
    }
}
