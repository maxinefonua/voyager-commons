package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;

import java.util.List;
import java.util.StringJoiner;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;
import static org.voyager.utils.ConstantsUtils.CONTINENT_PARAM_NAME;

public class CountryService {
    private final String servicePath;

    CountryService(@NonNull VoyagerConfig voyagerConfig) {
        servicePath = voyagerConfig.getCountryPath();
    }

    public Either<ServiceError, List<Country>> getCountries() {
        return fetch(servicePath, HttpMethod.GET,new TypeReference<List<Country>>(){});
    }

    public Either<ServiceError, List<Country>> getCountries(List<Continent> continentList) {
        StringJoiner countryJoiner = new StringJoiner(",");
        continentList.forEach(continent -> countryJoiner.add(continent.name()));
        String requestURL = servicePath.concat(String.format("?%s=%s",CONTINENT_PARAM_NAME,countryJoiner));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<List<Country>>(){});
    }

    public Either<ServiceError, Country> getCountry(String countryCode) {
        String requestURL = servicePath.concat(String.format("/%s",countryCode));
        return fetch(requestURL, HttpMethod.GET, Country.class);
    }

    public Either<ServiceError, Country> addCountry(CountryForm countryForm) {
        return fetchWithRequestBody(servicePath,HttpMethod.POST,Country.class,countryForm);
    }
}
