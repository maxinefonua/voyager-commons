package org.voyager.service.currency;

import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

public class XeCurrencyService {
    private static final String baseURL = "https://www.xe.com";
    private static final String currencyPath = "/currency";
    private static final Logger LOGGER = LoggerFactory.getLogger(XeCurrencyService.class);

    private static Map<String,String> codeToEndpointMap = extractCodeToEndpointMap();

    private static Map<String, String> extractCodeToEndpointMap() {
        try {
            Document doc = Jsoup.connect(baseURL.concat(currencyPath)).timeout(0).get();
            Elements scriptTags = doc.select("script[type=application/json]");
            String jsonData = scriptTags.get(0).html();
            JSONObject jsonObject = new JSONObject(jsonData);
            Map<String,Object> map = jsonObject.getJSONObject("props").getJSONObject("pageProps")
                    .getJSONObject("codeToSlugMap").toMap();
            return map.entrySet().stream().collect(Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> entry.getValue().toString()
            ));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String extractCurrencySymbol(String code) {
        if (!codeToEndpointMap.containsKey(code)) return code;
        String currencyURL = baseURL.concat(currencyPath).concat(String.format("/%s",codeToEndpointMap.get(code)));
        try {
            Document doc = Jsoup.connect(currencyURL).timeout(0).get();
            Elements scriptTags = doc.select("script[type=application/json]");
            String jsonData = scriptTags.get(0).html();
            JSONObject jsonObject = new JSONObject(jsonData);
            String fetchedSymbol = jsonObject.getJSONObject("props").getJSONObject("pageProps")
                    .getJSONObject("primaryUnitConfig").getString("symbol");
            if (StringUtils.isBlank(fetchedSymbol)) {
                LOGGER.error(String.format("extracted blank symbol for '%s' from XeCurrency data: %s, " +
                                "returning symbol: '%s'",
                        code,jsonData,code));
                return code;
            }
            return fetchedSymbol;
        } catch (IOException e) {
            LOGGER.error(String.format("Error fetching currency '%s' from %s, message: %s, returning symbol: '%s'",
                    code,currencyURL,e.getMessage(),code));
            return code;
        }
    }

}
