package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import jakarta.validation.Valid;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.CountryQuery;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import org.voyager.utils.Constants;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.util.List;
import java.util.StringJoiner;

public interface CountryService {
    Either<ServiceError, List<Country>> getCountries(CountryQuery countryQuery);
    Either<ServiceError, Country> getCountry(String countryCode);
    Either<ServiceError, Country> addCountry(@NonNull @Valid CountryForm countryForm);
}
