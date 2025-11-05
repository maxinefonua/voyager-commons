package org.voyager.commons.model.airline;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.geoname.fields.SearchOperator;
import org.voyager.commons.validate.annotations.ValidAirportCode;
import org.voyager.commons.validate.ValidationUtils;
import java.util.List;
import java.util.StringJoiner;

@Getter  @Setter
public class AirlineQuery {
    @NotEmpty
    private List<@ValidAirportCode(caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE)
            String> IATAList;

    private SearchOperator operator;

    private AirlineQuery(List<String> IATAList, SearchOperator operator) {
        this.IATAList = IATAList;
        if (operator == null) this.operator = SearchOperator.OR;
        else this.operator = operator;
    }

    public static AirlineQueryBuilder builder() {
        return new AirlineQueryBuilder();
    }

    public static class AirlineQueryBuilder {
       List<String> IATAList;
        SearchOperator operator;

        public AirlineQueryBuilder withIATAList(@NonNull List<String> IATAList) {
            this.IATAList = IATAList;
            return this;
        }

        public AirlineQueryBuilder withOperator(@NonNull SearchOperator operator) {
            this.operator = operator;
            return this;
        }

        public AirlineQuery build() {
            AirlineQuery query = new AirlineQuery(IATAList,operator);
            ValidationUtils.validateAndThrow(query);
            query.IATAList = query.IATAList.stream().map(String::toUpperCase).toList();
            return query;
        }
    }

    public String getRequestURL() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Path.AIRLINES);

        StringJoiner iataJoiner = new StringJoiner(",");
        IATAList.forEach(iataJoiner::add);
        urlBuilder.append(String.format("?%s=%s", ParameterNames.IATA_PARAM_NAME,iataJoiner));
        urlBuilder.append(String.format("&%s=%s",ParameterNames.OPERATOR,operator));
        return urlBuilder.toString();
    }
}
