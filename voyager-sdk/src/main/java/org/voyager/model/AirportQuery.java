package org.voyager.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.voyager.model.airport.AirportType;
import org.voyager.utils.Constants;

import java.util.List;
import java.util.StringJoiner;

public class AirportQuery {
    private String countryCode;
    private Airline airline;
    private List<AirportType> airportTypeList;

    private AirportQuery(String countryCode,Airline airline, List<AirportType> airportTypeList) {
        this.countryCode = countryCode;
        this.airline = airline;
        this.airportTypeList = airportTypeList;
    }

    public static String resolveRequestURL(AirportQuery airportQuery) {
        if (airportQuery == null) return Constants.Voyager.Path.AIRPORTS;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.Voyager.Path.AIRPORTS);
        urlBuilder.append("?");

        StringJoiner paramsJoiner = new StringJoiner("&");

        String countryCode = airportQuery.countryCode;
        if (StringUtils.isNotBlank(countryCode)) {
            paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.COUNTRY_CODE_PARAM_NAME,countryCode));
        }
        Airline airline = airportQuery.airline;
        if (airline != null) {
            paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }

        List<AirportType> airportTypeList = airportQuery.airportTypeList;
        if (airportTypeList != null && !airportTypeList.isEmpty()) {
            StringJoiner typeJoiner = new StringJoiner(",");
            airportTypeList.forEach(airportType -> typeJoiner.add(airportType.name()));
            paramsJoiner.add(String.format("%s=%s", Constants.Voyager.ParameterNames.TYPE_PARAM_NAME,typeJoiner));
        }

        urlBuilder.append(paramsJoiner);
        return urlBuilder.toString();
    }

    public static AirportQueryBuilder builder() {
        return new AirportQueryBuilder();
    }

    public static class AirportQueryBuilder {
        private Airline airline;
        private String countryCode;
        private List<AirportType> airportTypeList;

        public AirportQueryBuilder withAirline(@NonNull Airline airline) {
            this.airline = airline;
            return this;
        }

        public AirportQueryBuilder withCountryCode(@NotBlank @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA2_CODE_REGEX, message = Constants.Voyager.ConstraintMessage.COUNTRY_CODE)
                                                   String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public AirportQueryBuilder withTypeList(@NotEmpty @Valid List<@NonNull AirportType> airportTypeList) {
            this.airportTypeList = airportTypeList;
            return this;
        }

        public AirportQuery build() {
            return new AirportQuery(countryCode, airline, airportTypeList);
        }
    }
}
