package org.voyager.sdk.model;

import lombok.Getter;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.validate.annotations.NonNullElements;
import java.util.List;
import java.util.StringJoiner;

@Getter
public class IataQuery {
    @NonNullElements(message = "must be a nonempty list of valid airlines") // allows null List
    private final List<Airline> airlineList;

    @NonNullElements(message = "must be a nonempty list of valid airport types") // allows null List
    private final List<AirportType> airportTypeList;

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
            paramJoiner.add(String.format("%s=%s", ParameterNames.AIRLINE_PARAM_NAME,airlineJoiner));
        }
        if (airportTypeList != null) {
            StringJoiner typeJoiner = new StringJoiner(",");
            airportTypeList.forEach(airportType -> typeJoiner.add(airportType.name()));
            paramJoiner.add(String.format("%s=%s", ParameterNames.TYPE_PARAM_NAME,typeJoiner));
        }
        return String.format("%s?%s", Path.IATA,paramJoiner);
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
