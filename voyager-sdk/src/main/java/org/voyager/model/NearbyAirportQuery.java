package org.voyager.model;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.model.airport.AirportType;
import org.voyager.model.validate.NonNullElements;
import org.voyager.utils.Constants;
import org.voyager.utils.JakartaValidationUtil;

import java.util.List;
import java.util.StringJoiner;

public class NearbyAirportQuery {
    @Getter
    @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0")
    private Double latitude;

    @Getter
    @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0")
    private Double longitude;

    @Getter
    @Min(1)
    private Integer limit;

    @Getter
    @Size(min = 1,message = "cannot be empty") // allows null List
    @NonNullElements // allows for null List
    private List<Airline> airlineList;

    @Getter
    @Size(min = 1,message = "cannot be empty") // allows null List
    @NonNullElements // allows for null List
    private List<AirportType> airportTypeList;

    private NearbyAirportQuery(@NonNull Double latitude, @NonNull Double longitude,
                               Integer limit, List<Airline> airlineList, List<AirportType> airportTypeList) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.limit = limit;
        this.airlineList = airlineList;
        this.airportTypeList = airportTypeList;
    }

    public String getRequestURL() {
        StringJoiner paramsJoiner = new StringJoiner("&");
        paramsJoiner.add(String.format("%s" + "?%s=%s", Constants.Voyager.Path.NEARBY_AIRPORTS,
                Constants.Voyager.ParameterNames.LATITUDE_PARAM_NAME,latitude));
        paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.LONGITUDE_PARAM_NAME,longitude));

        if (limit != null) {
            paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.LIMIT_PARAM_NAME,limit));
        }

        if (airlineList != null) {
            StringJoiner airlineJoiner = new StringJoiner(",");
            airlineList.forEach(airline -> airlineJoiner.add(airline.name()));
            paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME, airlineJoiner));
        }

        if (airportTypeList != null) {
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

        public NearbyAirportQueryBuilder withAirlineList(@NonNull List<Airline> airlineList) {
            this.airlineList = airlineList;
            return this;
        }

        public NearbyAirportQueryBuilder withAirportTypeList(@NonNull List<AirportType> airportTypeList) {
            this.airportTypeList = airportTypeList;
            return this;
        }

        public NearbyAirportQuery build() {
            NearbyAirportQuery nearbyAirportQuery =
                    new NearbyAirportQuery(latitude,longitude,limit,airlineList,airportTypeList);
            JakartaValidationUtil.validate(nearbyAirportQuery);
            return nearbyAirportQuery;
        }
    }
}
