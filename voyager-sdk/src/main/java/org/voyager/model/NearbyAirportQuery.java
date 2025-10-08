package org.voyager.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.model.airport.AirportType;
import org.voyager.utils.Constants;

import java.util.List;
import java.util.StringJoiner;

public class NearbyAirportQuery {
    @Getter
    private Double latitude;
    @Getter
    private Double longitude;
    @Getter
    private Integer limit;
    @Getter
    private List<Airline> airlineList;
    @Getter
    private List<AirportType> airportTypeList;

    private NearbyAirportQuery(@NonNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
                               @NonNull @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double longitude,
                               Integer limit, List<Airline> airlineList, List<AirportType> airportTypeList) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.limit = limit;
        this.airlineList = airlineList;
        this.airportTypeList = airportTypeList;
    }

    public static String resolveRequestURL(@NonNull NearbyAirportQuery nearbyAirportQuery) {
        Double latitude = nearbyAirportQuery.getLatitude();
        Double longitude = nearbyAirportQuery.getLongitude();
        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s" + "?%s=%s", Constants.Voyager.Path.NEARBY_AIRPORTS,
                Constants.Voyager.ParameterNames.LATITUDE_PARAM_NAME,latitude));
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.LONGITUDE_PARAM_NAME,longitude));

        Integer limit = nearbyAirportQuery.getLimit();
        if (limit != null) {
            paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.LIMIT_PARAM_NAME,limit));
        }

        List<Airline> airlineList = nearbyAirportQuery.getAirlineList();
        if (airlineList != null && !airlineList.isEmpty()) {
            StringJoiner airlineJoiner = new StringJoiner(",");
            airlineList.forEach(airline -> airlineJoiner.add(airline.name()));
            paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME, airlineJoiner));
        }

        List<AirportType> airportTypeList = nearbyAirportQuery.getAirportTypeList();
        if (airportTypeList != null && !airportTypeList.isEmpty()) {
            StringJoiner typeJoiner = new StringJoiner(",");
            airportTypeList.forEach(airportType -> typeJoiner.add(airportType.name()));
            paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.TYPE_PARAM_NAME, typeJoiner));
        }

        return paramsJoiner.toString();
    }

    public static NearbyAirportQueryBuilder builder() {
        return new NearbyAirportQueryBuilder();
    }

    public static class NearbyAirportQueryBuilder {
        private Double latitude;
        private Double longitude;
        private Integer limit;
        private List<Airline> airlineList;
        private List<AirportType> airportTypeList;

        public NearbyAirportQueryBuilder withLatitude(@NonNull Double latitude) {
            this.latitude = latitude;
            return this;
        }

        public NearbyAirportQueryBuilder withLongitude(@NonNull Double longitude) {
            this.longitude = longitude;
            return this;
        }

        public NearbyAirportQueryBuilder withLimit(@NonNull Integer limit) {
            this.limit = limit;
            return this;
        }

        public NearbyAirportQueryBuilder withAirlineList(@NotEmpty @Valid List<@NonNull Airline> airlineList) {
            this.airlineList = airlineList;
            return this;
        }

        public NearbyAirportQueryBuilder withTypeList(@NonNull @Valid List<@NonNull AirportType> airportTypeList) {
            this.airportTypeList = airportTypeList;
            return this;
        }

        public NearbyAirportQuery build() {
            return new NearbyAirportQuery(latitude,longitude,limit,airlineList,airportTypeList);
        }
    }
}
