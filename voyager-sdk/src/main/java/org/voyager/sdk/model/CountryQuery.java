package org.voyager.sdk.model;

import lombok.Getter;
import lombok.NonNull;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.constants.Path;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.validate.annotations.NonNullElements;
import org.voyager.sdk.utils.JakartaValidationUtil;
import java.util.List;
import java.util.StringJoiner;

public class CountryQuery {
    @Getter
    @NonNullElements(message = "must be a nonempty list of valid continents") // allows null List
    private List<Continent> continentList;

    CountryQuery(@NonNull List<Continent> continentList) {
        this.continentList = continentList;
    }

    public static CountryQueryBuilder builder() {
        return new CountryQueryBuilder();
    }

    public String getRequestURL() {
        StringJoiner continentJoiner = new StringJoiner(",");
        continentList.forEach(continent -> continentJoiner.add(continent.name()));
        return String.format("%s?" + "%s=%s", Path.COUNTRIES,
                ParameterNames.COUNTRY_CODE_PARAM_NAME,continentJoiner);
    }

    public static class CountryQueryBuilder {
        private List<Continent> continentList;

        public CountryQueryBuilder withContinentList(@NonNull List<Continent> continentList) {
            this.continentList = continentList;
            return this;
        }

        public CountryQuery build() {
            CountryQuery countryQuery = new CountryQuery(this.continentList);
            JakartaValidationUtil.validate(countryQuery);
            return countryQuery;
        }
    }
}
