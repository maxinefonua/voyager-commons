package org.voyager.model;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.model.airline.Airline;
import org.voyager.model.airport.AirportType;
import org.voyager.model.validate.annotations.NonNullElements;
import org.voyager.utils.Constants;

import java.util.List;
import java.util.StringJoiner;

public class IataQuery {
    @Getter
    @Size(min = 1, message = "cannot be empty") // allows null List
    @NonNullElements // allows null List
    private List<Airline> airlineList;

    @Getter
    @Size(min = 1, message = "cannot be empty") // allows null List
    @NonNullElements // allows null List
    private List<AirportType> airportTypeList;

    IataQuery(List<Airline> airlineList, List<AirportType> airportTypeList) {
        this.airlineList = airlineList;
        this.airportTypeList = airportTypeList;
        if (airlineList == null && airportTypeList == null)
            throw new IllegalArgumentException("at least one field of IataQuery must be set");
    }

    public String getRequestURL() {
        StringJoiner paramJoiner = new StringJoiner("&");
        if (airlineList != null) {
            StringJoiner airlineJoiner = new StringJoiner(",");
            airlineList.forEach(airline -> airlineJoiner.add(airline.name()));
            paramJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.AIRLINE_PARAM_NAME,airlineJoiner));
        }
        if (airportTypeList != null) {
            StringJoiner typeJoiner = new StringJoiner(",");
            airportTypeList.forEach(airportType -> typeJoiner.add(airportType.name()));
            paramJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.TYPE_PARAM_NAME,typeJoiner));
        }
        return String.format("%s?%s", Constants.Voyager.Path.IATA,paramJoiner);
    }

    public static IataQueryBuilder builder(){
        return new IataQueryBuilder();
    }

    public static class IataQueryBuilder {
        private List<Airline> airlineList;
        private List<AirportType> airportTypeList;

        public IataQueryBuilder withAirlineList(@NonNull List<Airline> airlineList) {
            this.airlineList = airlineList;
            return this;
        }

        public IataQueryBuilder withAirportTypeList(@NonNull List<AirportType> airportTypeList) {
            this.airportTypeList = airportTypeList;
            return this;
        }

        public IataQuery build() {
            return new IataQuery(airlineList,airportTypeList);
        }
    }
}
