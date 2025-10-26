package org.voyager.sdk.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.model.CountryQuery;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import java.util.List;

public interface CountryService {
    Either<ServiceError, List<Country>> getCountries();
    Either<ServiceError, List<Country>> getCountries(@NonNull CountryQuery countryQuery);
    Either<ServiceError, Country> getCountry(@NotBlank String countryCode);
    Either<ServiceError, Country> addCountry(@NonNull @Valid CountryForm countryForm);
}
