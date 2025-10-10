package org.voyager.model;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.model.validate.AllStringsMatchRegex;
import org.voyager.utils.Constants;
import org.voyager.utils.JakartaValidationUtil;
import java.util.List;
import java.util.StringJoiner;

public class AirlineQuery {
    @Getter
    @Size(min = 1,message = "cannot be empty") // allows null List
    @AllStringsMatchRegex(regexp = Constants.Voyager.Regex.IATA_CODE_ALPHA3,
            message = Constants.Voyager.ConstraintMessage.IATA_CODE_ELEMENTS) // allows null List, excludes null elements
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
        urlBuilder.append(Constants.Voyager.Path.AIRLINES);
        urlBuilder.append("?");

        StringJoiner iataJoiner = new StringJoiner(",");
        IATAList.forEach(iataJoiner::add);
        urlBuilder.append(String.format("%s=%s", Constants.Voyager.ParameterNames.IATA_PARAM_NAME,iataJoiner));
        return urlBuilder.toString();
    }
}
