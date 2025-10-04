package org.voyager.service.model;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.voyager.model.Airline;
import org.voyager.model.airport.AirportType;

import java.util.List;
import java.util.StringJoiner;

import static org.voyager.utils.ConstantsUtils.*;
import static org.voyager.utils.ConstantsUtils.TYPE_PARAM_NAME;

public class AirportQuery {
    public static final String AIRPORTS_PATH = "/airports";

    @Getter
    private String countryCode;
    @Getter
    private Airline airline;
    @Getter
    private List<AirportType> airportTypeList;

    private AirportQuery(String countryCode,Airline airline, List<AirportType> airportTypeList) {
        this.countryCode = countryCode;
        this.airline = airline;
        this.airportTypeList = airportTypeList;
    }

    public static String resolveRequestURL(AirportQuery airportQuery) {
        if (airportQuery == null) return AIRPORTS_PATH;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(AIRPORTS_PATH);
        urlBuilder.append("?");

        StringJoiner paramsJoiner = new StringJoiner("&");

        String countryCode = airportQuery.getCountryCode();
        if (StringUtils.isNotBlank(countryCode)) {
            paramsJoiner.add(String.format("%s=%s", COUNTRY_CODE_PARAM_NAME,countryCode));
        }
        Airline airline = airportQuery.getAirline();
        if (airline != null) {
            paramsJoiner.add(String.format("%s=%s",AIRLINE_PARAM_NAME,airline.name()));
        }

        List<AirportType> airportTypeList = airportQuery.getAirportTypeList();
        if (airportTypeList != null && !airportTypeList.isEmpty()) {
            StringJoiner typeJoiner = new StringJoiner(",");
            airportTypeList.forEach(airportType -> typeJoiner.add(airportType.name()));
            paramsJoiner.add(String.format("%s=%s",TYPE_PARAM_NAME,typeJoiner));
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

        public AirportQueryBuilder withCountryCode(@NonNull String countryCode) {
            this.countryCode = countryCode;
            return this;
        }

        public AirportQueryBuilder withTypeList(@NonNull List<AirportType> airportTypeList) {
            this.airportTypeList = airportTypeList;
            return this;
        }

        public AirportQuery build() {
            return new AirportQuery(countryCode, airline, airportTypeList);
        }
    }
}
