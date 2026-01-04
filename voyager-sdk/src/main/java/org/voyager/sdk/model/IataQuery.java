package org.voyager.sdk.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.validate.annotations.ValidNonNullField;
import java.util.List;
import java.util.StringJoiner;

@Getter @ValidNonNullField
@Builder
public class IataQuery {
    private final List<@NotNull Airline> airlineList;
    private final List<@NotNull AirportType> airportTypeList;

    IataQuery(List<Airline> airlineList, List<AirportType> airportTypeList) {
        this.airlineList = airlineList;
        this.airportTypeList = airportTypeList;
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
}
