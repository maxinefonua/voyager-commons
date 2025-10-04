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
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsDefault;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;
import java.util.StringJoiner;

import static org.voyager.utils.ConstantsUtils.CONTINENT_PARAM_NAME;
import static org.voyager.utils.ConstantsUtils.CURRENCY_CODE_PARAM_NAME;

public class CountryService {
    private static final String COUNTRY_PATH = "/countries";
    private final ServiceUtils serviceUtils;

    CountryService() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    CountryService(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    public Either<ServiceError, List<Country>> getCountries() {
        return serviceUtils.fetch(COUNTRY_PATH, HttpMethod.GET,new TypeReference<List<Country>>(){});
    }

    public Either<ServiceError, List<Country>> getCountries(String currencyCode) {
        String requestURL = COUNTRY_PATH.concat(String.format("?%s=%s",CURRENCY_CODE_PARAM_NAME,currencyCode));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<List<Country>>(){});
    }

    public Either<ServiceError, List<Country>> getCountries(List<Continent> continentList) {
        StringJoiner countryJoiner = new StringJoiner(",");
        continentList.forEach(continent -> countryJoiner.add(continent.name()));
        String requestURL = COUNTRY_PATH.concat(String.format("?%s=%s",CONTINENT_PARAM_NAME,countryJoiner));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<List<Country>>(){});
    }

    public Either<ServiceError, Country> getCountry(String countryCode) {
        String requestURL = COUNTRY_PATH.concat(String.format("/%s",countryCode));
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Country.class);
    }

    public Either<ServiceError, Country> addCountry(CountryForm countryForm) {
        return serviceUtils.fetchWithRequestBody(COUNTRY_PATH,HttpMethod.POST,Country.class,countryForm);
    }
}
