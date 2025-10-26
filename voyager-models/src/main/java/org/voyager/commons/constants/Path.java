package org.voyager.commons.constants;

public final class Path {
    public static final String IATA = "/iata";
    public static final String AIRLINES = "/airlines";
    public static final String AIRPORTS = "/airports";
    @SuppressWarnings("unused")
    public static final String AIRPORT_BY_IATA = "/airports/{iata}";
    public static final String NEARBY_AIRPORTS = "/nearby-airports";
    public static final String COUNTRIES = "/countries";
    @SuppressWarnings("unused")
    public static final String COUNTRY_BY_CODE = "/countries/{code}";
    public static final String FLIGHTS = "/flights";
    @SuppressWarnings("unused")
    public static final String FLIGHT_BY_ID = "/flights/{id}";
    public static final String FLIGHT = "/flight";
    public static final String AIRLINE_PATH = "/airline-path";
    public static final String ROUTE_PATH = "/route-path";
    public static final String ROUTES = "/routes";
    @SuppressWarnings("unused")
    public static final String ROUTE_BY_ID = "/routes/{id}";
    public static final String ROUTE = "/route";

    public static final class Admin {
        public static final String HEALTH = "/admin/actuator/health";
        public static final String LOCATIONS = "/admin/locations";
        public static final String LOCATION = "/admin/location";
        @SuppressWarnings("unused")
        public static final String LOCATION_BY_ID = "/admin/locations/{id}";
        public static final String SEARCH = "/admin/search";
        public static final String ATTRIBUTION = "/admin/search/attribution";
        public static final String FETCH = "/admin/fetch";
        @SuppressWarnings("unused")
        public static final String FETCH_SOURCE_ID = "/admin/fetch/{sourceId}";
        public static final String FLIGHTS = "/admin/flights";
        @SuppressWarnings("unused")
        public static final String FLIGHT_BY_ID = "/admin/flights/{id}";
        public static final String COUNTRIES = "/admin/countries";
        public static final String AIRPORTS = "/admin/airports";
        @SuppressWarnings("unused")
        public static final String AIRPORT_BY_IATA = "/admin/airports/{iata}";
        public static final String AIRLINES = "/admin/airlines";
        public static final String ROUTES = "/routes";
        @SuppressWarnings("unused")
        public static final String ROUTE_BY_ID = "/admin/routes/{id}";
    }
}