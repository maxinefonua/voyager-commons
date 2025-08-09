package org.voyager.service.currency;

import io.vavr.control.Either;
import org.voyager.error.ServiceError;
import org.voyager.model.currency.CurrencyNames;
import org.voyager.model.currency.CurrencyRates;
import org.voyager.utils.HttpRequestUtils;

public class OpenExchangeRatesService {
    private static final String baseURL = "https://openexchangerates.org/api/";
    private static final String currenciesPath = "currencies.json";
    private static final String currenciesParams = "?app_id=%s";
    private static final String ratesPath = "latest.json";
    private static final String ratesParams = "?app_id=%s";
    private static final String OPEN_EXCHANGE_APP_ID = System.getenv("OPEN_EXCHANGE_APP_ID");

    public static Either<ServiceError, CurrencyNames> fetchCurrencyNames() {
        String requestURL = baseURL.concat(currenciesPath).concat(String.format(currenciesParams,OPEN_EXCHANGE_APP_ID));
        return HttpRequestUtils.getRequestBody(requestURL, CurrencyNames.class);
    }

    public static Either<ServiceError, CurrencyRates> fetchCurrencyRates() {
        String requestURL = baseURL.concat(ratesPath).concat(String.format(ratesParams,OPEN_EXCHANGE_APP_ID));
        return HttpRequestUtils.getRequestBody(requestURL, CurrencyRates.class);
    }
}
