package org.voyager.commons.model.flight;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.validate.annotations.ValidFlightNumber;

@SuperBuilder @Getter
public class FlightNumberQuery extends FlightQuery{
    @ValidFlightNumber
    String flightNumber;

    @Override
    public String getRequestURL() {
        String superRequestURL = super.getRequestURL();
        return String.format("%s&%s=%s", superRequestURL, ParameterNames.FLIGHT_NUMBER_PARAM_NAME,flightNumber);
    }
}
