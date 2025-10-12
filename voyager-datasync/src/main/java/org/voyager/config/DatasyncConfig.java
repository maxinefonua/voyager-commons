package org.voyager.config;

import org.junit.platform.commons.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.model.Airline;
import org.voyager.model.SyncStep;

import java.util.Set;
import java.util.StringJoiner;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public abstract class DatasyncConfig {
    public static class Flag {
        public static final String THREAD_COUNT = "-tc";
        public static final String HOSTNAME = "-h";
        public static final String PORT = "-p";
        public static final String AUTH_TOKEN = "-at";
        public static final String STEP_SYNC = "-st";
    }

    public static class Defaults {
        public static final int PORT = 3000;
        public static final int PORT_MAX = 65535;
        public static final int THREAD_COUNT = 10;
        public static final int THREAD_COUNT_MAX = 1000;
        public static final List<SyncStep> STEP_LIST_DEFAULT = List.of(SyncStep.ROUTES_SYNC,SyncStep.FLIGHTS_SYNC,SyncStep.AIRLINE_SYNC);
    }

    public static class Messages {
        private static final String MISSING_FLAG = "Missing required %s flag '%s'";
        public static String getMissingMessage(String flagName, String flagKey) {
            return String.format(MISSING_FLAG,flagName,flagKey);
        }

        private static final String INVALID_LIST = "Required %s flag '%s' must be a nonempty list from accepted values: %s";
        public static String getInvalidListMessage(String flagName, String flagKey, List<String> acceptableValueList) {
            StringJoiner valueJoiner = new StringJoiner(",");
            acceptableValueList.forEach(valueJoiner::add);
            return String.format(INVALID_LIST,flagName,flagKey,valueJoiner);
        }

        private static final String INVALID_VALUE = "Required %s flag '%s' has invalid value '%s'. Valid values must be from the following: %s";
        public static String getInvalidValueMessage(String flagName, String flagKey, String invalidValue, List<String> acceptableValueList) {
            StringJoiner valueJoiner = new StringJoiner(",");
            acceptableValueList.forEach(valueJoiner::add);
            return String.format(INVALID_VALUE,flagName,flagKey,invalidValue,valueJoiner);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasyncConfig.class);

    protected Map<String,Object> optionMap = new HashMap<>(Map.of(
            Flag.THREAD_COUNT,Defaults.THREAD_COUNT,
            Flag.PORT,Defaults.PORT,
            Flag.STEP_SYNC,Defaults.STEP_LIST_DEFAULT
    ));

    protected Map<String,Object> addtionalOptions = new HashMap<>();

    private Set<String> requiredFlags = Set.of(
            Flag.HOSTNAME,
            Flag.AUTH_TOKEN
    );

    public DatasyncConfig(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-")) {
                String[] tokens = arg.split("=");
                if (tokens.length != 2)
                    throw new RuntimeException(String.format("Malformatted program argument '%s'. Accepted format '-flag=VALUE'", arg));
                if (!optionMap.containsKey(tokens[0]) && !requiredFlags.contains(tokens[0])) {
                    addtionalOptions.put(tokens[0],tokens[1]);
                }
                processFlag(tokens[0],tokens[1]);
            } else {
                LOGGER.info(String.format("Ignoring unflagged program argument: %s",arg));
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

    public List<SyncStep> getStepList() {
        return ((List<SyncStep>) optionMap.get(Flag.STEP_SYNC));
    }

    public String[] toArgs() {
        List<String> argsList = new ArrayList<>();
        optionMap.forEach((flag, objectValue) -> {
            if (flag.equals(Flag.STEP_SYNC)) {
                StringJoiner stepJoiner = new StringJoiner(",");
                ((List<SyncStep>) optionMap.get(Flag.STEP_SYNC)).forEach(syncStep -> stepJoiner.add(syncStep.name()));
                argsList.add(flag.concat("=").concat(stepJoiner.toString()));
            } else {
                argsList.add(flag.concat("=").concat(objectValue.toString()));
            }
        });
        return argsList.toArray(new String[0]);
    }

    public VoyagerConfig getVoyagerConfig() {
        return new VoyagerConfig(Protocol.HTTP,this.getHostname(),this.getPort(),this.getAccessToken());
    }

    private void processFlag(String flag, String token) {
        switch (flag) {
            case Flag.THREAD_COUNT -> {
                int value = extractThreadCount(token);
                optionMap.put(Flag.THREAD_COUNT,value);
            }
            case Flag.HOSTNAME -> {
                optionMap.put(Flag.HOSTNAME,token);
            }
            case Flag.PORT -> {
                int value = extractPort(token);
                optionMap.put(Flag.PORT,value);
            }
            case Flag.AUTH_TOKEN -> {
                optionMap.put(Flag.AUTH_TOKEN,token);
            }
            case Flag.STEP_SYNC -> {
                List<SyncStep> stepList = Arrays.stream(token.split(","))
                        .map(step -> SyncStep.valueOf(step.toUpperCase())).toList();
                optionMap.put(Flag.STEP_SYNC,stepList);
            }
        }
    }

    private int extractPort(String token) {
        int value = parseIntegerFromValue(token, Flag.PORT);
        if (value > Defaults.PORT_MAX || value < 0 ) {
            throw new RuntimeException(String.format("Invalid port '%d' for '%s' flag. " +
                    "Acceptable values range from [0,65535]",value, Flag.PORT));
        }
        LOGGER.debug(String.format("Setting port to provided value %d", value));
        return value;
    }

    private static int extractThreadCount(String token) {
        int value = parseIntegerFromValue(token, Flag.THREAD_COUNT);
        if (value > Defaults.THREAD_COUNT_MAX) {
            LOGGER.info(String.format("Provided thread count %d exceeds maximum. " +
                            "Setting thread count to maximum: %d", value,Defaults.THREAD_COUNT_MAX));
            return Defaults.THREAD_COUNT_MAX;
        } else if (value < 1) {
            LOGGER.info(String.format("Provided thread count %d is fewer than allowed. " +
                            "Setting thread count to %d", value,1));
            return 1;
        }
        LOGGER.debug(String.format("Setting thread count to provided value %d", value));
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
