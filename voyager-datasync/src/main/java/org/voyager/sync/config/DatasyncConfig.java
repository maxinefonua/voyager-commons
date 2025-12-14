package org.voyager.sync.config;

import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.sdk.config.Protocol;
import org.voyager.sdk.config.VoyagerConfig;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.Collection;

public abstract class DatasyncConfig {
    public static class Flag {
        public static final String THREAD_COUNT = "-tc";
        public static final String HOSTNAME = "-h";
        public static final String PORT = "-p";
        public static final String AUTH_TOKEN = "-at";
        public static final String GEONAMES_USERNAME = "-gn";
        public static final String SYNC_MODE = "-sy";
    }

    public static class Defaults {
        public static final int PORT = 3000;
        public static final int PORT_MAX = 65535;
        public static final int THREAD_COUNT = 10;
        public static final int THREAD_COUNT_MAX = 1000;
    }

    public static class Messages {
        private static final String MISSING_FLAG = "required %s flag '%s' is missing";
        public static String getMissingMessage(String flagName, String flagKey) {
            return String.format(MISSING_FLAG,flagName,flagKey);
        }

        private static final String INVALID_LIST = "%s flag '%s' must be a nonempty list of valid values: %s";
        public static String getInvalidListMessage(String flagName, String flagKey, List<String> acceptableValueList) {
            StringJoiner valueJoiner = new StringJoiner(",");
            acceptableValueList.forEach(valueJoiner::add);
            return String.format(INVALID_LIST,flagName,flagKey,valueJoiner);
        }

        private static final String LIST_REGEX = "%s flag '%s' requires a nonempty list where each value %s";
        public static String getEmptyListConstraintElements(String flagName, String flagKey, String elemConstraint) {
            return String.format(LIST_REGEX,flagName,flagKey,elemConstraint);
        }

        private static final String INVALID_LIST_REGEX = "%s flag '%s' requires a nonempty list where each value %s, but contains invalid value: '%s'";
        public static String getInvalidListConstraintViolation(String flagName, String flagKey, String elemConstraint, String invalidValue) {
            return String.format(INVALID_LIST_REGEX,flagName,flagKey,elemConstraint,invalidValue);
        }

        private static final String INVALID_VALUE = "%s flag '%s' requires a valid %s, but contains invalid value: '%s'";
        public static String getInvalidValueMessage(String flagName, String flagKey, String valueConstraint, String invalidValue) {
            return String.format(INVALID_VALUE,flagName,flagKey,valueConstraint,invalidValue);
        }

        private static final String INVALID_VALUE_LIST = "%s flag '%s' has invalid value '%s'. Valid values must be from the following: %s";
        public static String getInvalidValueListValidMessage(String flagName, String flagKey, String invalidValue, List<String> acceptableValueList) {
            StringJoiner valueJoiner = new StringJoiner(",");
            acceptableValueList.forEach(valueJoiner::add);
            return String.format(INVALID_VALUE_LIST,flagName,flagKey,invalidValue,valueJoiner);
        }

        private static final String BLANK_VALUE = "%s flag '%s' cannot be blank.";
        public static String getBlankValueMessage(String flagName, String flagKey) {
            return String.format(BLANK_VALUE,flagName,flagKey);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasyncConfig.class);

    protected Map<String,Object> optionMap = new HashMap<>(Map.of(
            Flag.THREAD_COUNT,Defaults.THREAD_COUNT,
            Flag.PORT,Defaults.PORT
    ));

    protected Map<String,Object> additionalOptions = new HashMap<>();

    protected void validateGeoNamesUser() {
        if (!this.additionalOptions.containsKey(Flag.GEONAMES_USERNAME)) {
            throw new RuntimeException(DatasyncConfig.Messages.getMissingMessage("GeoNames username",
                    Flag.GEONAMES_USERNAME));
        }
        String value = (String) this.additionalOptions.get(Flag.GEONAMES_USERNAME);
        if (StringUtils.isBlank(value)) {
            throw new RuntimeException(DatasyncConfig.Messages.getBlankValueMessage("GeoNames username",
                    Flag.GEONAMES_USERNAME));
        }
    }

    public DatasyncConfig(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-")) {
                String[] tokens = arg.split("=");
                if (tokens.length != 2)
                    throw new RuntimeException(String.format("Malformed program argument '%s'. Accepted format '-flag=VALUE'", arg));
                processFlag(tokens[0],tokens[1]);
            } else {
                LOGGER.info("ignoring un-flagged program argument: {}", arg);
            }
        }
        if (!optionMap.containsKey(Flag.HOSTNAME)) {
            throw new RuntimeException(Messages.getMissingMessage("hostname", Flag.HOSTNAME));
        }
        if (!optionMap.containsKey(Flag.AUTH_TOKEN)) {
            throw new RuntimeException(Messages.getMissingMessage("API authorization flag", Flag.AUTH_TOKEN));
        }
    }

    public int getThreadCount() {
        return (int) optionMap.get(Flag.THREAD_COUNT);
    }

    public String getHostname() {
        return (String) optionMap.get(Flag.HOSTNAME);
    }

    public String getAccessToken() {
        return (String) optionMap.get(Flag.AUTH_TOKEN);
    }

    public int getPort() {
        return (int) optionMap.get(Flag.PORT);
    }

    public void setThreadCountMax(int threadCountMax) {
        optionMap.put(Flag.THREAD_COUNT,threadCountMax);
    }

    public String[] toArgs() {
        List<String> argsList = new ArrayList<>();
        optionMap.forEach((flag, objectValue) -> argsList.add(flag.concat("=").concat(objectValue.toString())));
        additionalOptions.forEach((flag, objectValue)-> {
            if (objectValue instanceof Collection<?>) {
                StringJoiner elemJoiner = new StringJoiner(",");
                ((Collection<?>) objectValue).forEach(elem -> elemJoiner.add(elem.toString()));
                argsList.add(String.format("%s=%s",flag,elemJoiner));
            } else {
                argsList.add(String.format("%s=%s", flag, objectValue.toString()));
            }
        });
        return argsList.toArray(new String[0]);
    }

    public VoyagerConfig getVoyagerConfig() {
        if (this.getHostname().equalsIgnoreCase("localhost")) {
            return new VoyagerConfig(Protocol.HTTP,this.getHostname(),this.getPort(),this.getAccessToken());
        } else {
            return new VoyagerConfig(Protocol.HTTPS,this.getHostname(),this.getAccessToken());
        }
    }

    private void processFlag(String flag, String token) {
        switch (flag) {
            case Flag.THREAD_COUNT -> {
                int value = extractThreadCount(token);
                optionMap.put(Flag.THREAD_COUNT,value);
            }
            case Flag.HOSTNAME -> optionMap.put(Flag.HOSTNAME,token);
            case Flag.PORT -> {
                int value = extractPort(token);
                optionMap.put(Flag.PORT,value);
            }
            case Flag.AUTH_TOKEN -> optionMap.put(Flag.AUTH_TOKEN,token);
            default -> additionalOptions.put(flag,token);
        }
    }

    private int extractPort(String token) {
        int value = parseIntegerFromValue(token, Flag.PORT);
        if (value > Defaults.PORT_MAX || value < 0 ) {
            throw new RuntimeException(String.format("Invalid port '%d' for '%s' flag. " +
                    "Acceptable values range from [0,65535]",value, Flag.PORT));
        }
        LOGGER.debug("Setting port to provided value {}", value);
        return value;
    }

    private static int extractThreadCount(String token) {
        int value = parseIntegerFromValue(token, Flag.THREAD_COUNT);
        if (value > Defaults.THREAD_COUNT_MAX) {
            LOGGER.info("provided thread count {} exceeds maximum. Setting thread count to maximum: {}",
                    value, Defaults.THREAD_COUNT_MAX);
            return Defaults.THREAD_COUNT_MAX;
        } else if (value < 1) {
            LOGGER.info("Provided thread count {} is fewer than allowed. Setting thread count to {}", value, 1);
            return 1;
        }
        LOGGER.debug("Setting thread count to provided value {}", value);
        return value;
    }

    private static Integer parseIntegerFromValue(String value, String flag) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Invalid argument '%s' for '%s' flag. " +
                    "Must be a valid integer",value,flag));
        }
    }
}
