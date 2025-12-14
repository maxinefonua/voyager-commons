package org.voyager.commons.constants;

@SuppressWarnings("unused")
public final class GeoNames {
    public static final String GEONAMES = "/admin/geonames";
    public static final String NEARBY_PLACES = "/nearby";
    public static final String TIMEZONE = "/timezone";
    public static final String SEARCH = "/search";
    public static final String FETCH = "/fetch";
    public static final String FETCH_BY_ID = "/fetch/{id}";
    public static final String COUNTRIES = "/countries";

    public static String getSearchPath() {
        return GEONAMES.concat(SEARCH);
    }

    public static String getNearbyPath() {
        return GEONAMES.concat(NEARBY_PLACES);
    }

    public static String getTimezonePath() {
        return GEONAMES.concat(TIMEZONE);
    }

    public static String getFetchPath() {
        return GEONAMES.concat(FETCH);
    }

    public static String getCountriesPath() {
        return GEONAMES.concat(COUNTRIES);
    }

    public static final class ParameterNames {
        public static final String QUERY = "q";
        public static final String RADIUS = "radius";
        public static final String LANGUAGE = "lang";
        public static final String DATE = "date";
        public static final String NAME = "name";
        public static final String NAME_EQUALS = "name_equals";
        public static final String NAME_STARTS_WITH = "name_startsWith";
        public static final String MAX_ROWS = "maxRows";
        public static final String START_ROW = "startRow";
        public static final String COUNTRY = "country";
        public static final String COUNTRY_BIAS = "countryBias";
        public static final String CONTINENT_CODE = "continentCode";
        public static final String ADMIN_CODE1 = "adminCode1";
        public static final String ADMIN_CODE2 = "adminCode2";
        public static final String ADMIN_CODE3 = "adminCode3";
        public static final String ADMIN_CODE4 = "adminCode4";
        public static final String ADMIN_CODE5 = "adminCode5";
        public static final String FEATURE_CLASS = "featureClass";
        public static final String FEATURE_CODE = "featureCode";
        public static final String CITIES = "cities";
        public static final String TYPE = "type";
        public static final String STYLE = "style";
        public static final String IS_NAME_REQUIRED = "isNameRequired";
        public static final String TAG = "tag";
        public static final String OPERATOR = "operator";
        public static final String CHARSET = "charset";
        public static final String FUZZY = "fuzzy";
        public static final String EAST = "east";
        public static final String WEST = "west";
        public static final String NORTH = "north";
        public static final String SOUTH = "south";
        public static final String SEARCH_LANG = "searchlang";
        public static final String ORDER_BY = "orderby";
        public static final String INCL_BBOX = "inclBbox";
    }
}
