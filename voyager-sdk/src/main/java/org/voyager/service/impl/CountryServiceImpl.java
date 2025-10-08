package org.voyager.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.CountryQuery;
import org.voyager.model.airport.Airport;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import org.voyager.service.CountryService;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;
import java.util.StringJoiner;

public class CountryServiceImpl implements CountryService {
    private final ServiceUtils serviceUtils;

    CountryServiceImpl() {
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    CountryServiceImpl(ServiceUtils serviceUtils) {
        this.serviceUtils = serviceUtils;
    }

    @Override
    public Either<ServiceError, List<Country>> getCountries(CountryQuery countryQuery) {
        String requestURL = CountryQuery.resolveRequestURL(countryQuery);
        return serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<List<Country>>(){});
    }

    @Override
    public Either<ServiceError, Country> getCountry(String countryCode) {
        String requestURL = Constants.Voyager.Path.COUNTRIES.concat(String.format("/%s",countryCode));
        return serviceUtils.fetch(requestURL, HttpMethod.GET, Country.class);    }

    @Override
    public Either<ServiceError, Country> addCountry(@NonNull @Valid CountryForm countryForm) {
        return serviceUtils.fetchWithRequestBody(Constants.Voyager.Path.COUNTRIES,HttpMethod.POST,Country.class,countryForm);
    }
}
