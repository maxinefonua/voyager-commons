package org.voyager.sdk.model;

import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.validate.annotations.NonNullElements;
import org.voyager.commons.validate.annotations.ValidCountryCode;
import org.voyager.sdk.utils.JakartaValidationUtil;
import java.util.List;
import java.util.StringJoiner;

public class AirportQuery {
    @Getter
    @ValidCountryCode(allowNull = true)
    private String countryCode;

    @Getter
    private Airline airline;

    @Getter
    @NonNullElements(message = "must be a nonempty list of valid airport types") // allows null List
    private List<AirportType> airportTypeList;

    private AirportQuery(String countryCode,Airline airline, List<AirportType> airportTypeList) {
        this.countryCode = countryCode;
        this.airline = airline;
        this.airportTypeList = airportTypeList;
        if (countryCode == null && airline == null && airportTypeList == null)
            throw new IllegalArgumentException("at least one field of AirportQuery must be set");
    }

    public String getRequestURL() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Path.AIRPORTS);
        urlBuilder.append("?");

        StringJoiner paramsJoiner = new StringJoiner("&");

        if (StringUtils.isNotBlank(countryCode)) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.COUNTRY_CODE_PARAM_NAME,countryCode));
        }
        if (airline != null) {
            paramsJoiner.add(String.format("%s=%s", ParameterNames.AIRLINE_PARAM_NAME,airline.name()));
        }
        if (airportTypeList != null) {
            StringJoiner typeJoiner = new StringJoiner(",");
            airportTypeList.forEach(airportType -> typeJoiner.add(airportType.name()));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.TYPE_PARAM_NAME,typeJoiner));
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
            this.countryCode = countryCode.toUpperCase();
            return this;
        }

        public AirportQueryBuilder withTypeList(@NonNull List<AirportType> airportTypeList) {
            this.airportTypeList = airportTypeList;
            return this;
        }

        public AirportQuery build() {
            AirportQuery airportQuery = new AirportQuery(countryCode, airline, airportTypeList);
            JakartaValidationUtil.validate(airportQuery);
            if (airportQuery.countryCode != null)
                airportQuery.countryCode = airportQuery.countryCode.toUpperCase();
            return airportQuery;
        }
    }
}
