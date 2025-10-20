package org.voyager.service;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import org.voyager.config.external.NominatimConfig;
import org.voyager.error.HttpStatus;
import org.voyager.error.ServiceError;
import org.voyager.error.ServiceException;
import org.voyager.model.nominatim.FeatureSearch;
import org.voyager.utils.HttpRequestUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class NominatimService {
    private static NominatimConfig nominatimConfig = new NominatimConfig();

    public static Either<ServiceError, FeatureSearch> searchCountryName(String countryName) {
        String encodedCountryName = URLEncoder.encode(countryName, StandardCharsets.UTF_8);
        String requestURL = String.format("%s/%s?%s",nominatimConfig.getBaseURL(),nominatimConfig.getSearchPath(),
                String.format(nominatimConfig.getSearchParams(),encodedCountryName));
        return HttpRequestUtils.getRequestBody(requestURL,new TypeReference<List<FeatureSearch>>(){}).flatMap(
                featureSearchList -> {
                    if (featureSearchList.size() > 1) {
                        return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                                new ServiceException(String.format("Multiple results returned from requestURL: %s",requestURL))));
                    }
                    if (featureSearchList.isEmpty()) {
                        return Either.left(new ServiceError(HttpStatus.INTERNAL_SERVER_ERROR,
                                new ServiceException(String.format("No results returned from requestURL: %s",requestURL))));
                    }
                    return Either.right(featureSearchList.get(0));
                }
        );
    }
}
