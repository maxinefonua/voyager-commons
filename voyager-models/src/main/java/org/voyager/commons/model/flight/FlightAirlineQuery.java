package org.voyager.commons.model.flight;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.model.airline.Airline;

import java.util.List;

@SuperBuilder @Getter
public class FlightAirlineQuery extends FlightQuery {
    @NotEmpty
    private List<Airline> airlineList;

    @Override
    public String getRequestURL() {
        String superRequestURL = super.getRequestURL();
        return String.format("%s&%s=%s",superRequestURL,
                ParameterNames.AIRLINE,
                String.join(",",airlineList.stream().map(Airline::name).toList()));
    }
}
