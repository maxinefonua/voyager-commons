package org.voyager.commons.constants;

public final class Regex {
    public static final String COUNTRY_CODE = "^[A-Z]{2}$";
    public static final String COUNTRY_CODE_CASE_INSENSITIVE = "^[a-zA-Z]{2}$";

    public static final String NOEMPTY_NOWHITESPACE = "^\\S+$";
    public static final String NONEMPTY_TRIMMED = "^\\S+(?:\\s+\\S+)*$";

    public static final String AIRPORT_CODE = "^[A-Z]{3}$";
    public static final String AIRPORT_CODE_CASE_INSENSITIVE = "^[a-zA-Z]{3}$";

    public static final String FLIGHT_NUMBER = "^(?:[A-Z]{2,3}|[A-Z][0-9]|[0-9][A-Z])[0-9]{1,4}[A-Z]?$";

    public static final class ConstraintMessage {
        public static final String AIRPORT_CODE = "must be a valid three-letter uppercase ISO 3166-1 alpha-2 country code";
        public static final String AIRPORT_CODE_CASE_INSENSITIVE = "must be a valid three-letter uppercase ISO 3166-1 alpha-2 country code";
        public static final String AIRPORT_CODE_ELEMENTS = "all elements must be valid three-letter uppercase airport IATA code";
        public static final String AIRPORT_CODE_ELEMENTS_CASE_INSENSITIVE = "all elements must be valid three-letter airport IATA code";
        public static final String AIRPORT_CODE_ELEMENTS_NONEMPTY = "must be a nonempty list of valid three-letter uppercase airport IATA codes";
        public static final String AIRPORT_CODE_ELEMENTS_NONEMPTY_CASE_INSENSITIVE = "must be a nonempty list of valid three-letter airport IATA codes";

        public static final String COUNTRY_CODE = "must be a valid two-letter uppercase ISO 3166-1 alpha-2 country code";
        public static final String COUNTRY_CODE_CASE_INSENSITIVE = "must be a valid two-letter ISO 3166-1 alpha-2 country code";
        public static final String COUNTRY_CODE_ELEMENTS = "all elements must be valid two-letter uppercase ISO 3166-1 alpha-2 country codes";
        public static final String COUNTRY_CODE_ELEMENTS_NONEMPTY = "must be a nonempty list of valid two-letter uppercase country codes";
        public static final String COUNTRY_CODE_ELEMENTS_NONEMPTY_CASE_INSENSITIVE = "must be a nonempty list of valid two-letter country codes";

        public static final String FLIGHT_NUMBER = "must be a valid flight number";
        public static final String FLIGHT_NUMBER_ELEMENTS = "all elements must be valid flight numbers";
        public static final String FLIGHT_NUMBER_ELEMENTS_NONEMPTY = "must be a nonempty list of valid flight numbers";


        public static final String NOEMPTY_NOWHITESPACE = "cannot be empty or contain whitespaces";
        public static final String NONEMPTY_TRIMMED = "cannot be empty or contain leading or trailing whitespace";
    }
}
