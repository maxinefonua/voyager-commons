package org.voyager.model.airport;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.voyager.utils.MapperUtils;

@Builder(toBuilder = true) @Data
@AllArgsConstructor @NoArgsConstructor
@ToString(includeFieldNames = false)
@EqualsAndHashCode(exclude = "distance")
public class Airport {
    @NonNull
    String iata;
    @NonNull
    String name;
    String city;
    String subdivision;
    @NonNull
    String countryCode;
    @NonNull
    Double latitude;
    @NonNull
    Double longitude;
    @NonNull
    AirportType type;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Double distance;

    private static final MapperUtils<Airport> mapper = new MapperUtils<>(Airport.class);
    private static final int EARTH_RADIUS = 6371;

    private static double haversine(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    public static double calculateDistance(double startLat,double startLong, double endLat, double endLong) {
        double dLat = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat = Math.toRadians(endLat);

        double a = haversine(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversine(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }

    public String toJson() {
        return mapper.mapToJson(this);
    }
}
