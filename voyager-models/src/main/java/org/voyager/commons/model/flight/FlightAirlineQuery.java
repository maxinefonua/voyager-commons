package org.voyager.commons.model.flight;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.model.airline.Airline;

@SuperBuilder @Getter
public class FlightAirlineQuery extends FlightQuery {
    @NotNull
    private Airline airline;

    @Override
    public String getRequestURL() {
        String superRequestURL = super.getRequestURL();
        return String.format("%s&%s=%s", superRequestURL,ParameterNames.AIRLINE_PARAM_NAME,airline.name());
    }
}
