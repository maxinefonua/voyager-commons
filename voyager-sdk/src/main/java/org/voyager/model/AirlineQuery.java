package org.voyager.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.utils.Constants;

import java.util.List;
import java.util.StringJoiner;

public class AirlineQuery {
    private List<String> iataList;

    AirlineQuery(@NonNull List<String> iataList) {
        this.iataList = iataList;
    }

    public static AirlineQueryBuilder builder() {
        return new AirlineQueryBuilder();
    }

    public static class AirlineQueryBuilder {
        private List<String> iataList;

        public AirlineQueryBuilder withIataList(@NotEmpty @Valid List<@NonNull @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA3_CODE_REGEX,message = Constants.Voyager.ConstraintMessage.IATA_CODE)
                String> iataList) {
            this.iataList = iataList;
            return this;
        }

        public AirlineQuery build() {
            return new AirlineQuery(iataList);
        }
    }

    public static String resolveRequestURL(@NonNull AirlineQuery airlineQuery) {
        if (airlineQuery == null) return Constants.Voyager.Path.AIRLINES;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.Voyager.Path.AIRLINES);
        urlBuilder.append("?");

        List<String> iataList = airlineQuery.iataList;
        if (iataList != null && !iataList.isEmpty()) {
            StringJoiner iataJoiner = new StringJoiner(",");
            iataList.forEach(iataJoiner::add);
            urlBuilder.append(String.format("%s=%s", Constants.Voyager.ParameterNames.IATA_PARAM_NAME,iataJoiner));
        }
        return urlBuilder.toString();
    }
}
