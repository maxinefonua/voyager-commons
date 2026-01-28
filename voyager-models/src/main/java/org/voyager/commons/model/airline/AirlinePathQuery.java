package org.voyager.commons.model.airline;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidAirportCode;

import java.util.List;
import java.util.StringJoiner;


@Builder @Getter
public class AirlinePathQuery extends AirlineQuery {
    @NotEmpty
    private List<@ValidAirportCode(caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE)
            String> originList;

    @NotEmpty
    private List<@ValidAirportCode(caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE)
            String> destinationList;

    @Override
    public String getRequestURL() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Path.AIRLINES);

        StringJoiner originJoiner = new StringJoiner(",");
        originList.forEach(originJoiner::add);
        urlBuilder.append(String.format("?%s=%s", ParameterNames.ORIGIN,originJoiner));

        StringJoiner destinationJoiner = new StringJoiner(",");
        destinationList.forEach(destinationJoiner::add);
        urlBuilder.append(String.format("?%s=%s", ParameterNames.DESTINATION,destinationJoiner));
        return urlBuilder.toString();
    }
}
