package org.voyager.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.model.country.Continent;
import org.voyager.utils.Constants;

import java.util.List;
import java.util.StringJoiner;

public class CountryQuery {
    @Getter
    private List<Continent> continentList;

    CountryQuery(List<Continent> continentList) {
        this.continentList = continentList;
    }

    public static CountryQueryBuilder builder() {
        return new CountryQueryBuilder();
    }

    public static String resolveRequestURL(CountryQuery countryQuery) {
        if (countryQuery == null || countryQuery.getContinentList() == null
                || countryQuery.getContinentList().isEmpty()) return Constants.Voyager.Path.COUNTRIES;
        StringJoiner continentJoiner = new StringJoiner(",");
        countryQuery.getContinentList().forEach(continent -> continentJoiner.add(continent.name()));
        return String.format("%s?" + "%s=%s",Constants.Voyager.Path.COUNTRIES,
                Constants.Voyager.ParameterNames.COUNTRY_CODE_PARAM_NAME,continentJoiner);
    }

    public static class CountryQueryBuilder {
        private List<Continent> continentList;

        public CountryQueryBuilder withContinentList(@NotEmpty @Valid List<@NonNull Continent> continentList) {
            this.continentList = continentList;
            return this;
        }

        public CountryQuery build() {
            return new CountryQuery(this.continentList);
        }
    }
}
