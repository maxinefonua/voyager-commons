package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.voyager.commons.constants.Path;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.sdk.model.CountryQuery;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.sdk.service.CountryService;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;
import java.util.List;

public class CountryServiceImpl implements CountryService {
    private final ServiceUtils serviceUtils;

    CountryServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    CountryServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    @Override
    public Either<ServiceError, List<Country>> getCountries() {
        String requestURL = Path.COUNTRIES;
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<List<Country>>(){});
    }

    @Override
    public Either<ServiceError, List<Country>> getCountries(@NonNull CountryQuery countryQuery) {
        return serviceUtils.fetch(countryQuery.getRequestURL(), HttpMethod.GET,new TypeReference<List<Country>>(){});
    }

    @Override
    public Either<ServiceError, Country> getCountry(@NotBlank String countryCode) {
        String requestURL = Path.COUNTRIES.concat(String.format("/%s",countryCode));
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Country.class);    }

    @Override
    public Either<ServiceError, Country> addCountry(@NonNull @Valid CountryForm countryForm) {
        return serviceUtils.fetchWithRequestBody(Path.Admin.COUNTRIES,HttpMethod.POST,Country.class,countryForm);
    }
}
