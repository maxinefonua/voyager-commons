package org.voyager.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.model.country.Continent;
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.Source;
import org.voyager.model.location.Status;
import org.voyager.utils.Constants;

import java.util.List;
import java.util.StringJoiner;

public class LocationQuery {
    private Source source;
    private Integer limit;
    private List<String> countryCodeList;
    private List<Status> statusList;
    private List<Continent> continentList;

    LocationQuery(Source source, Integer limit, List<String> countryCodeList,
                  List<Status> statusList, List<Continent> continentList) {
        this.source = source;
        this.limit = limit;
        this.countryCodeList = countryCodeList;
        this.statusList = statusList;
        this.continentList = continentList;
    }

    public static LocationQueryBuilder builder() {
        return new LocationQueryBuilder();
    }

    public static String resolveRequestURL(LocationQuery locationQuery) {
        if (locationQuery == null) return Constants.Voyager.Path.LOCATIONS;
        StringJoiner paramsJoiner = new StringJoiner("&");

        Source source = locationQuery.source;
        if (source != null) paramsJoiner.add(String.format("%s=%s",
                Constants.Voyager.ParameterNames.SOURCE_PARAM_NAME,source.name()));

        Integer limit = locationQuery.limit;
        if (limit != null) paramsJoiner.add(String.format("%s=%d",
                Constants.Voyager.ParameterNames.LIMIT_PARAM_NAME,limit));

        List<String> countryCodeList = locationQuery.countryCodeList;
        if (countryCodeList != null && !countryCodeList.isEmpty()) {
            StringJoiner countryJoiner = new StringJoiner(",");
            countryCodeList.forEach(countryJoiner::add);
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.COUNTRY_CODE_PARAM_NAME,countryJoiner));
        }

        List<Status> statusList = locationQuery.statusList;
        if (statusList != null && !statusList.isEmpty()) {
            StringJoiner statusJoiner = new StringJoiner(",");
            statusList.forEach(status->statusJoiner.add(status.name()));
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.LOCATION_STATUS_PARAM_NAME,statusJoiner));
        }

        List<Continent> continentList = locationQuery.continentList;
        if (continentList != null && !continentList.isEmpty()) {
            StringJoiner continentJoiner = new StringJoiner(",");
            continentList.forEach(continent->continentJoiner.add(continent.name()));
            paramsJoiner.add(String.format("%s=%s",
                    Constants.Voyager.ParameterNames.CONTINENT_PARAM_NAME,continentJoiner));
        }

        if (paramsJoiner.length() == 0) return Constants.Voyager.Path.LOCATIONS;
        return String.format("%s?%s",Constants.Voyager.Path.LOCATIONS,paramsJoiner);
    }

    public static class LocationQueryBuilder {
        private Source source;
        private Integer limit;
        private List<String> countryCodeList;
        private List<Status> statusList;
        private List<Continent> continentList;

        public LocationQueryBuilder withSource(@NonNull Source source) {
            this.source = source;
            return this;
        }

        public LocationQueryBuilder withLimit(@NonNull Integer limit) {
            this.limit = limit;
            return this;
        }

        public LocationQueryBuilder withCountryCodeList(@NotEmpty @Valid List<@NonNull @Pattern(regexp =
                Constants.Voyager.Regex.ALPHA2_CODE_REGEX, message = Constants.Voyager.ConstraintMessage.COUNTRY_CODE)
                String> countryCodeList) {
            this.countryCodeList = countryCodeList;
            return this;
        }

        public LocationQueryBuilder withStatusList(@NotEmpty @Valid List<@NonNull Status> statusList) {
            this.statusList = statusList;
            return this;
        }

        public LocationQueryBuilder withContinentList(@NotEmpty @Valid List<@NonNull Continent> continentList) {
            this.continentList = continentList;
            return this;
        }

        public LocationQuery build() {
            return new LocationQuery(source,limit,countryCodeList,statusList,continentList);
        }
    }
}
