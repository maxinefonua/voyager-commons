package org.voyager.service.currency;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.utils.HttpRequestUtils;

import java.util.List;

public class APIVerveService {
    private static final String baseURL = "https://api.apiverve.com/v1";
    private static final String symbolsPath = "/currencysymbols";
    private static final String symbolsParams = "?currency=%s";
    private static final String VERVE_API_KEY = System.getenv("VERVE_API_KEY");
    private static final String VERVE_API_HEADER = "x-api-key";
    private static final Logger LOGGER = LoggerFactory.getLogger(APIVerveService.class);

    public static String extractCurrencySymbol(String code) {
        String requestURL = baseURL.concat(symbolsPath).concat(String.format(symbolsParams,code));
        return HttpRequestUtils.getResponseBodyAsString(requestURL,List.of(VERVE_API_HEADER,VERVE_API_KEY))
                .fold(serviceError -> {
                    Exception exception = serviceError.getException();
                    LOGGER.error(String.format("exception while fetching symbol for '%s' from APIVerve, error: %s",code,exception.getMessage()));
                    return code;
                },jsonData -> {
                    JSONObject jsonObject = new JSONObject(jsonData);
                    if (jsonObject.isNull("data")
                            || jsonObject.getJSONObject("data").isNull("countriesFound")
                            || jsonObject.getJSONObject("data").getJSONArray("countriesFound").isEmpty()
                            || jsonObject.getJSONObject("data").getJSONArray("countriesFound")
                                .getJSONObject(0).isNull("currency_symbol")) {
                        LOGGER.error(String.format("fetched currency for '%s' from APIVerve missing data: %s, " +
                                        "returning symbol: '%s'", code,jsonObject,code));
                        return code;
                    }
                    String fetchedSymbol = jsonObject.getJSONObject("data").getJSONArray("countriesFound")
                            .getJSONObject(0).getString("currency_symbol");
                    if (StringUtils.isBlank(fetchedSymbol)) {
                        LOGGER.error(String.format("extracted blank symbol for '%s' from APIVerve data: %s, " +
                                        "returning symbol: '%s'", code,
                                jsonObject.getJSONObject("data").getJSONArray("countriesFound")
                                        .getJSONObject(0),code));
                        return code;
                    }
                    return fetchedSymbol;
                });
    }
}

