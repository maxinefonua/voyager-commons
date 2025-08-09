package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.voyager.config.VoyagerConfig;
import org.voyager.error.ServiceError;
import org.voyager.http.HttpMethod;
import org.voyager.model.currency.Currency;
import org.voyager.model.currency.CurrencyForm;
import org.voyager.model.currency.CurrencyPatch;

import java.util.List;

import static org.voyager.service.Voyager.fetch;
import static org.voyager.service.Voyager.fetchWithRequestBody;
import static org.voyager.utils.ConstantsUtils.IS_ACTIVE_PARAM_NAME;

public class CurrencyService {
    private final String servicePath;

    CurrencyService(@NonNull VoyagerConfig voyagerConfig) {
        this.servicePath = voyagerConfig.getCurrenciesPath();
    }

    public Either<ServiceError, List<Currency>> getCurrencies() {
        return fetch(servicePath, HttpMethod.GET,new TypeReference<List<Currency>>(){});
    }

    public Either<ServiceError, List<Currency>> getCurrencies(Boolean isActive) {
        String requestURL = servicePath.concat(String.format("?%s=%s",IS_ACTIVE_PARAM_NAME,isActive));
        return fetch(requestURL, HttpMethod.GET,new TypeReference<List<Currency>>(){});
    }

    public Either<ServiceError, Currency> getCurrency(String code) {
        String requestURL = servicePath.concat(String.format("/%s",code));
        return fetch(requestURL,HttpMethod.GET, Currency.class);
    }

    public Either<ServiceError,Currency> addCurrency(CurrencyForm currencyForm) {
        return fetchWithRequestBody(servicePath,HttpMethod.POST,Currency.class,currencyForm);
    }

    public Either<ServiceError,Currency> patchCurrency(String code, CurrencyPatch currencyPatch) {
        String requestURL = servicePath.concat(String.format("/%s",code));
        return fetchWithRequestBody(requestURL,HttpMethod.PATCH,Currency.class,currencyPatch);
    }
}
