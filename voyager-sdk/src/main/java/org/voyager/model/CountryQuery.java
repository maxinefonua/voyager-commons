package org.voyager.model;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NonNull;
import org.voyager.model.country.Continent;
import org.voyager.model.validate.NonNullElements;
import org.voyager.utils.Constants;
import org.voyager.utils.JakartaValidationUtil;
import java.util.List;
import java.util.StringJoiner;

public class CountryQuery {
    @Getter
    @NonNullElements // allows null List
    @Size(min = 1,message = "cannot be empty") // allows null List
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
        return String.format("%s?" + "%s=%s",Constants.Voyager.Path.COUNTRIES,
                Constants.Voyager.ParameterNames.COUNTRY_CODE_PARAM_NAME,continentJoiner);
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
