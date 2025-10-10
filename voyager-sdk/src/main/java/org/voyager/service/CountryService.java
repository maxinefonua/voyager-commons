package org.voyager.service;

import io.vavr.control.Either;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NonNull;
import org.voyager.error.ServiceError;
import org.voyager.model.CountryQuery;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import java.util.List;

public interface CountryService {
    Either<ServiceError, List<Country>> getCountries();
    Either<ServiceError, List<Country>> getCountries(@NonNull CountryQuery countryQuery);
    Either<ServiceError, Country> getCountry(@NotBlank String countryCode);
    Either<ServiceError, Country> addCountry(@NonNull @Valid CountryForm countryForm);
}
