package org.voyager.sdk.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import io.vavr.control.Either;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.constants.GeoNames;
import org.voyager.commons.constants.ParameterNames;
import org.voyager.commons.error.HttpStatus;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.error.ServiceException;
import org.voyager.sdk.http.HttpMethod;
import org.voyager.commons.model.geoname.GeoCountry;
import org.voyager.commons.model.geoname.GeoFull;
import org.voyager.commons.model.geoname.GeoPlace;
import org.voyager.commons.model.geoname.GeoTimezone;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import org.voyager.commons.model.geoname.response.GeoResponse;
import org.voyager.commons.model.geoname.response.GeoStatus;
import org.voyager.sdk.service.GeoService;
import org.voyager.sdk.utils.JakartaValidationUtil;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;
import java.util.List;

public class GeoServiceImpl implements GeoService {
    private static final Logger LOGGER = LoggerFactory.getLogger(GeoServiceImpl.class);
    private final ServiceUtils serviceUtils;

    GeoServiceImpl(){
        this.serviceUtils = ServiceUtilsFactory.getInstance();
    }

    GeoServiceImpl(ServiceUtils serviceUtils){
        this.serviceUtils = serviceUtils;
    }
    @Override
    public Either<ServiceError, List<GeoPlace>> findNearbyPlaces(@NonNull GeoNearbyQuery geoNearbyQuery) {
        JakartaValidationUtil.validate(geoNearbyQuery);
        String requestURL = String.format("%s?" + "%s=%s" + "&%s=%s",GeoNames.NEARBY_PLACES,
                ParameterNames.LATITUDE_PARAM_NAME,geoNearbyQuery.getLatitude(),
                ParameterNames.LONGITUDE_PARAM_NAME,geoNearbyQuery.getLongitude());
        if (geoNearbyQuery.getRadius() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.RADIUS,geoNearbyQuery.getRadius()));
        }
        LOGGER.debug(String.format("attempting to GET %s",requestURL));
        Either<ServiceError, GeoResponse<GeoPlace>> either = serviceUtils.fetch(requestURL, HttpMethod.GET,new TypeReference<GeoResponse<GeoPlace>>(){});
        if (either.isLeft()) return Either.left(either.getLeft());
        GeoResponse<GeoPlace> geoPlaceGeoResponse = either.get();
        if (geoPlaceGeoResponse.getGeoStatus() != null) {
            GeoStatus geoStatus = geoPlaceGeoResponse.getGeoStatus();
            return Either.left(new ServiceError(geoStatus.getValue().equals(19) ?
                    HttpStatus.TOO_MANY_REQUESTS : HttpStatus.INTERNAL_SERVER_ERROR,
                    new ServiceException(geoStatus.getMessage())));
        }
        return Either.right(geoPlaceGeoResponse.getResults());
    }

    @Override
    public Either<ServiceError, GeoTimezone> getTimezone(@NonNull GeoTimezoneQuery geoTimezoneQuery) {
        JakartaValidationUtil.validate(geoTimezoneQuery);
        String requestURL = String.format("%s?" + "%s=%s" + "&%s=%s",GeoNames.TIMEZONE,
                ParameterNames.LATITUDE_PARAM_NAME,geoTimezoneQuery.getLatitude(),
                ParameterNames.LONGITUDE_PARAM_NAME,geoTimezoneQuery.getLongitude());
        if (geoTimezoneQuery.getRadius() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.RADIUS,geoTimezoneQuery.getRadius()));
        }
        if (geoTimezoneQuery.getDate() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.DATE,geoTimezoneQuery.getDate()));
        }
        if (geoTimezoneQuery.getLanguage() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.LANGUAGE,geoTimezoneQuery.getLanguage()));
        }
        LOGGER.debug(String.format("attempting to GET %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,GeoTimezone.class);
    }

    @Override
    public Either<ServiceError, List<GeoPlace>> search(@NonNull GeoSearchQuery geoSearchQuery) {
        JakartaValidationUtil.validate(geoSearchQuery);
        String requestURL = String.format("%s?" + "%s=%s" + "&%s=%s" + "&%s=%s" + "%s=%s"
                        + "&%s=%s" + "&%s=%s" + "&%s=%s" + "&%s=%s",
                GeoNames.SEARCH,
                GeoNames.ParameterNames.QUERY,geoSearchQuery.getQuery(),
                GeoNames.ParameterNames.MAX_ROWS,geoSearchQuery.getMaxRows(),
                GeoNames.ParameterNames.START_ROW,geoSearchQuery.getStartRow(),
                GeoNames.ParameterNames.LANGUAGE,geoSearchQuery.getLang(),
                GeoNames.ParameterNames.TYPE,geoSearchQuery.getType(),
                GeoNames.ParameterNames.OPERATOR,geoSearchQuery.getOperator(),
                GeoNames.ParameterNames.CHARSET,geoSearchQuery.getCharSet(),
                GeoNames.ParameterNames.FUZZY,geoSearchQuery.getFuzzy());
        if (StringUtils.isNotBlank(geoSearchQuery.getName())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.NAME,
                    geoSearchQuery.getName()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getNameEquals())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.NAME_EQUALS,
                    geoSearchQuery.getNameEquals()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getNameStartsWith())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.NAME_STARTS_WITH,
                    geoSearchQuery.getNameStartsWith()));
        }
        if (geoSearchQuery.getCountryList() != null) {
            for (String country : geoSearchQuery.getCountryList()) {
                requestURL = requestURL.concat(String.format("&%s=%s",
                        GeoNames.ParameterNames.COUNTRY,country));
            }
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getCountryBias())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.COUNTRY_BIAS,
                    geoSearchQuery.getCountryBias()));
        }
        if (geoSearchQuery.getContinentCode() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.CONTINENT_CODE,
                    geoSearchQuery.getContinentCode().name()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode1())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.ADMIN_CODE1,
                    geoSearchQuery.getAdminCode1()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode2())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.ADMIN_CODE2,
                    geoSearchQuery.getAdminCode2()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode3())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.ADMIN_CODE3,
                    geoSearchQuery.getAdminCode3()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode4())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.ADMIN_CODE4,
                    geoSearchQuery.getAdminCode4()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode5())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.ADMIN_CODE5,
                    geoSearchQuery.getAdminCode5()));
        }
        if (geoSearchQuery.getFeatureClass() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.FEATURE_CLASS,
                    geoSearchQuery.getFeatureClass().name()));
        }
        if (geoSearchQuery.getFeatureCode() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.FEATURE_CODE,
                    geoSearchQuery.getFeatureCode().name()));
        }
        if (geoSearchQuery.getCities() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.CITIES,
                    geoSearchQuery.getCities().name()));
        }
        if (geoSearchQuery.getStyle() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.STYLE,
                    geoSearchQuery.getStyle().name()));
        }
        if (geoSearchQuery.getIsNameRequired() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.IS_NAME_REQUIRED,
                    geoSearchQuery.getIsNameRequired()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getTag())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.TAG,
                    geoSearchQuery.getTag()));
        }
        if (geoSearchQuery.getEast() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",GeoNames.ParameterNames.EAST,
                    geoSearchQuery.getEast()));
        }
        if (geoSearchQuery.getWest() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",GeoNames.ParameterNames.WEST,
                    geoSearchQuery.getWest()));
        }
        if (geoSearchQuery.getNorth() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",GeoNames.ParameterNames.NORTH,
                    geoSearchQuery.getNorth()));
        }
        if (geoSearchQuery.getSouth() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",GeoNames.ParameterNames.SOUTH,
                    geoSearchQuery.getSouth()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getSearchLang())) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.SEARCH_LANG,
                    geoSearchQuery.getSearchLang()));
        }
        if (geoSearchQuery.getOrderBy() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.ORDER_BY,
                    geoSearchQuery.getOrderBy().name()));
        }
        if (geoSearchQuery.getInclBbox() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",GeoNames.ParameterNames.INCL_BBOX,
                    geoSearchQuery.getInclBbox()));
        }
        LOGGER.debug(String.format("attempting to GET %s",requestURL));
        return serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<List<GeoPlace>>(){});
    }

    @Override
    public Either<ServiceError, GeoFull> getFull(@NonNull Long geoNameId) {
        String requestURL = String.format("%s/%d",GeoNames.FETCH,geoNameId);
        LOGGER.debug(String.format("attempting to GET %s",requestURL));
        return serviceUtils.fetch(requestURL, HttpMethod.GET,GeoFull.class);
    }

    @Override
    public Either<ServiceError, List<GeoCountry>> getCountries() {
        String requestURL = GeoNames.COUNTRIES;
        LOGGER.debug("attempting to GET {}",requestURL);
        Either<ServiceError,GeoResponse<GeoCountry>> either = serviceUtils.fetch(requestURL,HttpMethod.GET,new TypeReference<GeoResponse<GeoCountry>>(){});
        if (either.isLeft()) return Either.left(either.getLeft());
        GeoResponse<GeoCountry> geoCountryGeoResponse = either.get();
        if (geoCountryGeoResponse.getGeoStatus() != null) {
            GeoStatus geoStatus = geoCountryGeoResponse.getGeoStatus();
            return Either.left(new ServiceError(geoStatus.getValue().equals(19) ?
                    HttpStatus.TOO_MANY_REQUESTS : HttpStatus.INTERNAL_SERVER_ERROR,
                    new ServiceException(geoStatus.getMessage())));
        }
        return Either.right(geoCountryGeoResponse.getResults());
    }
}
