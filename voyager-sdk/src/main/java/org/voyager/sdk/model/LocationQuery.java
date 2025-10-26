package org.voyager.sdk.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.constants.Regex;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.location.Source;
import org.voyager.commons.model.location.Status;
import org.voyager.commons.validate.annotations.NonNullElements;
import org.voyager.commons.validate.annotations.ValidCountryCodeCollection;
import org.voyager.sdk.utils.JakartaValidationUtil;
import java.util.List;
import java.util.StringJoiner;

public class LocationQuery {
    @Getter
    private Source source;

    @Getter
    @Min(1)
    @Max(100)
    private Integer limit;

    @Getter
    @ValidCountryCodeCollection(allowNullCollection = true,
            allowEmptyCollection = false,
            caseSensitive = false,
            message = Regex.ConstraintMessage.COUNTRY_CODE_ELEMENTS_NONEMPTY_CASE_INSENSITIVE)
    private List<String> countryCodeList;

    @Getter
    @NonNullElements(message = "must be a nonempty list of valid status values") // allows null List
    private List<Status> statusList;

    @Getter
    @NonNullElements(message = "must be a nonempty list of valid continents")  // allows null List
    private List<Continent> continentList;

    LocationQuery(Source source, Integer limit, List<String> countryCodeList,
                  List<Status> statusList, List<Continent> continentList) {
        this.source = source;
        this.limit = limit;
        this.countryCodeList = countryCodeList;
        this.statusList = statusList;
        this.continentList = continentList;
        if (source == null && limit == null && countryCodeList == null && statusList == null && continentList == null)
            throw new IllegalArgumentException("at least one field of LocationQuery must be set");
    }

    public static LocationQueryBuilder builder() {
        return new LocationQueryBuilder();
    }

    public String getRequestURL() {
        StringJoiner paramsJoiner = new StringJoiner("&");

        if (source != null) paramsJoiner.add(String.format("%s=%s",
                ParameterNames.SOURCE_PARAM_NAME,source.name()));

        if (limit != null) paramsJoiner.add(String.format("%s=%d", ParameterNames.LIMIT_PARAM_NAME,limit));

        if (countryCodeList != null) {
            StringJoiner countryJoiner = new StringJoiner(",");
            countryCodeList.forEach(countryJoiner::add);
            paramsJoiner.add(String.format("%s=%s", ParameterNames.COUNTRY_CODE_PARAM_NAME,countryJoiner));
        }

        if (statusList != null) {
            StringJoiner statusJoiner = new StringJoiner(",");
            statusList.forEach(status->statusJoiner.add(status.name()));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.LOCATION_STATUS_PARAM_NAME,statusJoiner));
        }

        if (continentList != null) {
            StringJoiner continentJoiner = new StringJoiner(",");
            continentList.forEach(continent->continentJoiner.add(continent.name()));
            paramsJoiner.add(String.format("%s=%s", ParameterNames.CONTINENT_PARAM_NAME,continentJoiner));
        }

        return String.format("%s?%s",Path.Admin.LOCATIONS,paramsJoiner);
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

        public LocationQueryBuilder withCountryCodeList(@NonNull List<String> countryCodeList) {
            this.countryCodeList = countryCodeList;
            return this;
        }

        public LocationQueryBuilder withStatusList(@NonNull List<Status> statusList) {
            this.statusList = statusList;
            return this;
        }

        public LocationQueryBuilder withContinentList(@NonNull List<Continent> continentList) {
            this.continentList = continentList;
            return this;
        }

        public LocationQuery build() {
            LocationQuery locationQuery = new LocationQuery(source,limit,countryCodeList,statusList,continentList);
            JakartaValidationUtil.validate(locationQuery);
            if (countryCodeList != null) {
                locationQuery.countryCodeList = countryCodeList.stream().map(String::toUpperCase).toList();
            }
            return locationQuery;
        }
    }
}
