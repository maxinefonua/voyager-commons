package org.voyager.commons.constants;

public final class GeoNames {
    public static final String NEARBY_PLACES = "/admin/geonames/nearby";
    public static final String TIMEZONE = "/admin/geonames/timezone";
    public static final String SEARCH = "/admin/geonames/search";
    public static final String FETCH = "/admin/geonames/fetch";
    public static final String FETCH_BY_ID = "/admin/geonames/fetch/{id}";
    public static final String COUNTRIES = "/admin/geonames/countries";

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
