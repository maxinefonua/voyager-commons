package org.voyager.commons.model.airport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.NoArgsConstructor;
import lombok.ToString;
import java.time.ZoneId;

@Builder(toBuilder = true) @Data
@AllArgsConstructor @NoArgsConstructor
@ToString
public class Airport {
    @NonNull
    private String iata;
    @NonNull
    private String name;
    private String city;
    private String subdivision;
    @NonNull
    private String countryCode;
    @NonNull
    private Double latitude;
    @NonNull
    private Double longitude;
    @NonNull
    private AirportType type;
    private ZoneId zoneId;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Double distance;

    private static final int EARTH_RADIUS = 6371;

    private static double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public static double calculateDistanceKm(double startLat, double startLong, double endLat, double endLong) {
        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}
