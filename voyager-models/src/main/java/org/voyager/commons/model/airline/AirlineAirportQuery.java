package org.voyager.commons.model.airline;

import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.geoname.fields.SearchOperator;
import org.voyager.commons.validate.annotations.ValidAirportCode;

import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

@Builder @Getter @Setter
public class AirlineAirportQuery extends AirlineQuery {
    @NotEmpty
    private List<@ValidAirportCode(caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE)
            String> iatalist;

    @Builder.Default
    private SearchOperator operator = SearchOperator.OR;

    // Custom builder class
    public static class AirlineAirportQueryBuilder {
        private List<String> iatalist;

        public AirlineAirportQueryBuilder iatalist(@NonNull List<String> iatalist) {
            this.iatalist = iatalist.stream()
                    .map(String::toUpperCase)
                    .collect(Collectors.toList());
            return this;
        }
    }

    public String getRequestURL() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Path.AIRLINES);

        StringJoiner iataJoiner = new StringJoiner(",");
        iatalist.forEach(iataJoiner::add);
        urlBuilder.append(String.format("?%s=%s", ParameterNames.IATA_PARAM_NAME,iataJoiner));
        urlBuilder.append(String.format("&%s=%s",ParameterNames.OPERATOR,operator));
        return urlBuilder.toString();
    }
}
