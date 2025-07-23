package org.voyager.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.model.Airline;
import org.voyager.model.SyncStep;

import java.util.Set;
import java.util.StringJoiner;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class DatasyncProgramArguments {
    private static final int PORT_DEFAULT = 3000;
    private static final int PORT_MAX = 65535;
    private static final int THREAD_COUNT_DEFAULT = 100;
    private static final int THREAD_COUNT_MAX = 1000;
    private static final int LIMIT_DEFAULT = 1000;
    private static final Airline AIRLINE_DEFAULT = Airline.DELTA;

    public static final String THREAD_COUNT_FLAG = "-tc";
    public static final String HOSTNAME_FLAG = "-h";
    public static final String PORT_FLAG = "-p";
    public static final String AUTH_TOKEN_FLAG = "-at";
    public static final String STEP_FLAG = "-st";
    public static final String LIMIT_FLAG = "-l";
    public static final String AIRLINE_FLAG = "-a";
    private static final Logger LOGGER = LoggerFactory.getLogger(DatasyncProgramArguments.class);
    private static final List<SyncStep> STEP_LIST_DEFAULT = List.of(SyncStep.ROUTES_SYNC,SyncStep.FLIGHTS_SYNC,SyncStep.AIRLINE_SYNC);

    private Map<String,Object> flagMap = new HashMap<>(Map.of(
            THREAD_COUNT_FLAG,THREAD_COUNT_DEFAULT,
            PORT_FLAG,PORT_DEFAULT,
            LIMIT_FLAG,LIMIT_DEFAULT,
            AIRLINE_FLAG,AIRLINE_DEFAULT,
            STEP_FLAG,STEP_LIST_DEFAULT
    ));

    private Set<String> requiredFlags = Set.of(
            HOSTNAME_FLAG,
            AUTH_TOKEN_FLAG
    );

    public DatasyncProgramArguments(String[] args) {
        for (String arg : args) {
            if (arg.startsWith("-")) {
                String[] tokens = arg.split("=");
                if (tokens.length != 2)
                    throw new RuntimeException(String.format("Malformatted program argument '%s'. Accepted format '-flag=VALUE'", arg));
                if (!flagMap.containsKey(tokens[0]) && !requiredFlags.contains(tokens[0])) {
                    StringJoiner flags = new StringJoiner(",");
                    flagMap.keySet().forEach(flags::add);
                    requiredFlags.forEach(flags::add);
                    throw new RuntimeException(String.format("Unrecognized flag '%s'. Accepted flags: %s",
                            tokens[0],flags));
                }
                processFlag(tokens[0],tokens[1]);
            } else {
                LOGGER.info(String.format("Ignoring unflagged program argument: %s",arg));
            }
        }
        if (!flagMap.containsKey(HOSTNAME_FLAG)) {
            throw new RuntimeException(String.format("Missing required hostname flag '%s'",HOSTNAME_FLAG));
        }
        if (!flagMap.containsKey(AUTH_TOKEN_FLAG)) {
            throw new RuntimeException(String.format("Missing required Voyager authorization token flag '%s'", AUTH_TOKEN_FLAG));
        }
    }

    public int getThreadCount() {
        return (int) flagMap.get(THREAD_COUNT_FLAG);
    }

    public String getHostname() {
        return (String) flagMap.get(HOSTNAME_FLAG);
    }

    public String getAccessToken() {
        return (String) flagMap.get(AUTH_TOKEN_FLAG);
    }

    public int getPort() {
        return (int) flagMap.get(PORT_FLAG);
    }

    public int getProcessLimit() {
        return (int) flagMap.get(LIMIT_FLAG);
    }

    public Airline getAirline() {
        return (Airline) flagMap.get(AIRLINE_FLAG);
    }

    public void setThreadCountMax(int threadCountMax) {
        flagMap.put(THREAD_COUNT_FLAG,threadCountMax);
    }

    public List<SyncStep> getStepList() {
        return ((List<SyncStep>) flagMap.get(STEP_FLAG));
    }

    public String[] toArgs() {
        List<String> argsList = new ArrayList<>();
        flagMap.forEach((flagKey,objectValue) -> {
            if (flagKey.equals(STEP_FLAG)) {
                StringJoiner stringJoiner = new StringJoiner(",");
                ((List<SyncStep>)flagMap.get(STEP_FLAG)).forEach(syncStep -> stringJoiner.add(syncStep.name()));
                argsList.add(flagKey.concat("=").concat(stringJoiner.toString()));
            } else {
                argsList.add(flagKey.concat("=").concat(objectValue.toString()));
            }
        });
        return argsList.toArray(new String[0]);
    }

    public VoyagerConfig getVoyagerConfig() {
        return new VoyagerConfig(Protocol.HTTP,this.getHostname(),this.getPort(),
                this.getThreadCount(),this.getAccessToken());
    }

    private void processFlag(String flag, String token) {
        switch (flag) {
            case THREAD_COUNT_FLAG -> {
                int value = extractThreadCount(token);
                flagMap.put(THREAD_COUNT_FLAG,value);
            }
            case HOSTNAME_FLAG -> {
                flagMap.put(HOSTNAME_FLAG,token);
            }
            case PORT_FLAG -> {
                int value = extractPort(token);
                flagMap.put(PORT_FLAG,value);
            }
            case AUTH_TOKEN_FLAG -> {
                flagMap.put(AUTH_TOKEN_FLAG,token);
            }
            case LIMIT_FLAG -> {
                int value = extractProcessLimit(token);
                flagMap.put(LIMIT_FLAG,value);
            }
            case AIRLINE_FLAG -> {
                Airline airline = extractAirline(token);
                flagMap.put(AIRLINE_FLAG,airline);
            }
            case STEP_FLAG -> {
                List<SyncStep> stepList = Arrays.stream(token.split(","))
                        .map(step -> SyncStep.valueOf(step.toUpperCase())).toList();
                flagMap.put(STEP_FLAG,stepList);
            }
        }
    }

    private Airline extractAirline(String token) {
        try {
            return Airline.valueOf(token.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(String.format("Invalid value '%s' for airline flag '%s'",token,AIRLINE_FLAG));
        }
    }

    private int extractProcessLimit(String token) {
        int value = parseIntegerFromValue(token,LIMIT_FLAG);
        if (value < 1) {
            LOGGER.info(String.format("Provided limit %d is fewer than minimum of 1. Setting process limit to minimum: %d",
                    value,1));
            return 1;
        }
        LOGGER.debug(String.format("Setting process limit to provided value %d", value));
        return value;
    }

    private int extractPort(String token) {
        int value = parseIntegerFromValue(token,PORT_FLAG);
        if (value > PORT_MAX || value < 0 ) {
            throw new RuntimeException(String.format("Invalid port '%d' for '%s' flag. Acceptable values range from [0,65535]",value,PORT_FLAG));
        }
        LOGGER.debug(String.format("Setting port to provided value %d", value));
        return value;
    }

    private static int extractThreadCount(String token) {
        int value = parseIntegerFromValue(token,THREAD_COUNT_FLAG);
        if (value > THREAD_COUNT_MAX) {
            LOGGER.info(String.format("Provided thread count %d exceeds maximum. Setting thread count to maximum: %d",
                    value,THREAD_COUNT_MAX));
            return THREAD_COUNT_MAX;
        } else if (value < 1) {
            LOGGER.info(String.format("Provided thread count %d is fewer than allowed. Setting thread count to %d",
                    value,1));
            return 1;
        }
        LOGGER.debug(String.format("Setting thread count to provided value %d", value));
        return value;
    }

    private static Integer parseIntegerFromValue(String value, String flag) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new RuntimeException(String.format("Invalid argument '%s' for '%s' flag. Must be a valid integer",value,flag));
        }
    }
}
