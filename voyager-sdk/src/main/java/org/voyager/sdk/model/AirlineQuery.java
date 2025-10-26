package org.voyager.sdk.model;

import lombok.Getter;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.validate.annotations.ValidAirportCodeCollection;
import org.voyager.sdk.utils.JakartaValidationUtil;
import java.util.List;
import java.util.StringJoiner;

public class AirlineQuery {
    @Getter
    @ValidAirportCodeCollection(allowNullCollection = true,
            allowEmptyCollection = false,caseSensitive = false,
            message = Regex.ConstraintMessage.AIRPORT_CODE_ELEMENTS_NONEMPTY_CASE_INSENSITIVE)
    private List<String> IATAList;

    private AirlineQuery(@NonNull List<String> IATAList) {
        this.IATAList = IATAList;
    }

    public static AirlineQueryBuilder builder() {
        return new AirlineQueryBuilder();
    }

    public static class AirlineQueryBuilder {
       List<String> IATAList;

        public AirlineQueryBuilder withIATAList(@NonNull List<String> IATAList) {
            this.IATAList = IATAList;
            return this;
        }

        public AirlineQuery build() {
            AirlineQuery query = new AirlineQuery(IATAList);
            JakartaValidationUtil.validate(query);
            query.IATAList = query.IATAList.stream().map(String::toUpperCase).toList();
            return query;
        }
    }

    public String getRequestURL() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Path.AIRLINES);
        urlBuilder.append("?");

        StringJoiner iataJoiner = new StringJoiner(",");
        IATAList.forEach(iataJoiner::add);
        urlBuilder.append(String.format("%s=%s", ParameterNames.IATA_PARAM_NAME,iataJoiner));
        return urlBuilder.toString();
    }
}
