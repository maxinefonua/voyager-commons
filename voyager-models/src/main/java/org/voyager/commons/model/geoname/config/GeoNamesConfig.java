package org.voyager.commons.model.geoname.config;

import org.apache.commons.lang3.StringUtils;
import org.voyager.commons.constants.GeoNames;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.StringJoiner;

public class GeoNamesConfig {
    private static final String baseURL = "https://secure.geonames.org";
    private static final String nearbyPlacePathWithParams = baseURL.concat("/findNearbyPlaceNameJSON?username=%s&lat=%f&lng=%f");
    private static final String timezonePathWithParams = baseURL.concat("/timezoneJSON?username=%s&lat=%f&lng=%f");
    private static final String countryPathWithParams = baseURL.concat("/countryInfoJSON?username=%s");
    private static final String getPathWithParams = baseURL.concat("/getJSON?username=%s&geonameId=%d");
    private static final String searchWithParams = baseURL.concat("/searchJSON?username=%s&q=%s");
    private final String geoUsername;

    public GeoNamesConfig(String geoUsername) {
        this.geoUsername = geoUsername;
    }


    public String getNearbyPlaceURL(GeoNearbyQuery geoNearbyQuery){
        String requestURL = String.format(nearbyPlacePathWithParams,geoUsername,
                geoNearbyQuery.getLatitude(),geoNearbyQuery.getLongitude());
        if (geoNearbyQuery.getRadiusKm() != null) {
            requestURL = requestURL.concat(String.format("&%s=%d",
                    GeoNames.ParameterNames.RADIUS,geoNearbyQuery.getRadiusKm()));
        }
        return requestURL;
    }




    public String getTimezoneURL(GeoTimezoneQuery geoTimezoneQuery) {
        String requestURL = String.format(timezonePathWithParams,geoUsername,
                geoTimezoneQuery.getLatitude(), geoTimezoneQuery.getLongitude());
        if (StringUtils.isNotBlank(geoTimezoneQuery.getLanguage())){
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.LANGUAGE,geoTimezoneQuery.getLanguage()));
        }
        if (StringUtils.isNotBlank(geoTimezoneQuery.getDate())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.DATE,geoTimezoneQuery.getDate()));
        }
        if (geoTimezoneQuery.getRadius() != null) {
            requestURL = requestURL.concat(String.format("&%s=%d",
                    GeoNames.ParameterNames.RADIUS,geoTimezoneQuery.getRadius()));
        }
        return requestURL;
    }

    public String getCountriesURL(){
        return String.format(countryPathWithParams,geoUsername);
    }

    public String getByIdURL(Long geonameId){
        return String.format(getPathWithParams,geoUsername,geonameId);
    }

    public String getSearchURL(GeoSearchQuery geoSearchQuery) {
        String urlEncodedQuery = URLEncoder.encode(geoSearchQuery.getQuery(), StandardCharsets.UTF_8);

        String requestURL = String.format(searchWithParams,geoUsername,urlEncodedQuery);
        if (StringUtils.isNotBlank(geoSearchQuery.getName())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.NAME,geoSearchQuery.getName()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getNameEquals())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.NAME_EQUALS,geoSearchQuery.getNameEquals()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getNameStartsWith())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.NAME_STARTS_WITH,geoSearchQuery.getNameStartsWith()));
        }
        if (geoSearchQuery.getMaxRows() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.MAX_ROWS,geoSearchQuery.getMaxRows()));
        }
        if (geoSearchQuery.getStartRow() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.START_ROW,geoSearchQuery.getStartRow()));
        }
        if (geoSearchQuery.getCountryList() != null && !geoSearchQuery.getCountryList().isEmpty()) {
            StringJoiner stringJoiner = new StringJoiner("&");
            geoSearchQuery.getCountryList().forEach(country -> stringJoiner.add(String.format("%s=%s",
                    GeoNames.ParameterNames.COUNTRY, country)));
            requestURL = requestURL.concat(String.format("&%s", stringJoiner));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getCountryBias())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.COUNTRY_BIAS,geoSearchQuery.getCountryBias()));
        }
        if (geoSearchQuery.getContinentCode() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.CONTINENT_CODE,geoSearchQuery.getContinentCode().name()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode1())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.ADMIN_CODE1,geoSearchQuery.getAdminCode1()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode2())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.ADMIN_CODE2,geoSearchQuery.getAdminCode2()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode3())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.ADMIN_CODE3,geoSearchQuery.getAdminCode3()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode4())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.ADMIN_CODE4,geoSearchQuery.getAdminCode4()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getAdminCode5())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.ADMIN_CODE5,geoSearchQuery.getAdminCode5()));
        }
        if (geoSearchQuery.getFeatureClass() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.FEATURE_CLASS,geoSearchQuery.getFeatureClass().name()));
        }
        if (geoSearchQuery.getFeatureCodeList() != null && !geoSearchQuery.getFeatureCodeList().isEmpty()) {
            StringJoiner featureCodeJoiner = new StringJoiner("&");
            geoSearchQuery.getFeatureCodeList().forEach(featureCode -> featureCodeJoiner.add(
                    String.format("%s=%s",GeoNames.ParameterNames.FEATURE_CODE,featureCode.name())));
            requestURL = requestURL.concat(String.format("&%s",featureCodeJoiner));
        }
        if (geoSearchQuery.getCities() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.CITIES,geoSearchQuery.getCities().name()));
        }
        if (geoSearchQuery.getLang() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.LANGUAGE,geoSearchQuery.getLang()));
        }
        if (geoSearchQuery.getType() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.TYPE,geoSearchQuery.getType().name()));
        }
        if (geoSearchQuery.getStyle() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.STYLE,geoSearchQuery.getStyle().name()));
        }
        if (geoSearchQuery.getIsNameRequired() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.IS_NAME_REQUIRED, geoSearchQuery.getIsNameRequired()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getTag())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.TAG,geoSearchQuery.getTag()));
        }
        if (geoSearchQuery.getOperator() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.OPERATOR, geoSearchQuery.getOperator().name()));
        }
        if (geoSearchQuery.getCharSet() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.CHARSET, geoSearchQuery.getCharSet().name()));
        }
        if (geoSearchQuery.getFuzzy() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",
                    GeoNames.ParameterNames.FUZZY, geoSearchQuery.getFuzzy()));
        }
        if (geoSearchQuery.getEast() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",
                    GeoNames.ParameterNames.EAST, geoSearchQuery.getEast()));
        }
        if (geoSearchQuery.getWest() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",
                    GeoNames.ParameterNames.WEST, geoSearchQuery.getWest()));
        }
        if (geoSearchQuery.getSouth() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",
                    GeoNames.ParameterNames.SOUTH, geoSearchQuery.getSouth()));
        }
        if (geoSearchQuery.getNorth() != null) {
            requestURL = requestURL.concat(String.format("&%s=%f",
                    GeoNames.ParameterNames.NORTH, geoSearchQuery.getNorth()));
        }
        if (StringUtils.isNotBlank(geoSearchQuery.getSearchLang())) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.SEARCH_LANG, geoSearchQuery.getSearchLang()));
        }
        if (geoSearchQuery.getOrderBy() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.ORDER_BY, geoSearchQuery.getOrderBy().name()));
        }
        if (geoSearchQuery.getInclBbox() != null) {
            requestURL = requestURL.concat(String.format("&%s=%s",
                    GeoNames.ParameterNames.INCL_BBOX, geoSearchQuery.getInclBbox()));
        }
        return requestURL;
    }
}
