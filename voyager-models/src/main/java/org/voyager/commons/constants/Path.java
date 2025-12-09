package org.voyager.commons.constants;

public final class Path {
    public static final String IATA = "/iata";
    public static final String AIRLINES = "/airlines";
    public static final String AIRPORTS = "/airports";
    public static final String NEARBY_AIRPORTS = "/nearby-airports";
    public static final String COUNTRIES = "/countries";
    public static final String FLIGHTS = "/flights";
    public static final String FLIGHT = "/flight";
    public static final String ROUTES = "/routes";
    public static final String ROUTE = "/route";
    public static final String PATH = "/path";
    public static final String AIRPORT_BY_IATA = "/airports/{iata}";

    public static final String BY_IATA = "/{iata}";
    public static final String BY_ID = "/{id}";
    public static final String BY_COUNTRY_CODE = "/{countryCode}";
    public static final String FLIGHT_BY_ID = "/flights/{id}";

    public static final class Admin {
        public static final String HEALTH = "/admin/actuator/health";
        public static final String SEARCH = "/admin/search";
        public static final String ATTRIBUTION = "/admin/search/attribution";
        public static final String FETCH = "/admin/fetch";
        public static final String FETCH_SOURCE_ID = "/admin/fetch/{sourceId}";

        public static final String FLIGHTS = "/admin/flights";
        public static final String FLIGHT_BY_ID = "/admin/flights/{id}";

        public static final String COUNTRIES = "/admin/countries";

        public static final String AIRPORTS = "/admin/airports";

        public static final String AIRLINES = "/admin/airlines";
        public static final String ROUTES = "/admin/routes";
        public static final String ROUTE_BY_ID = "/admin/routes/{id}";
    }
}