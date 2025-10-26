package org.voyager.sync.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.model.airline.Airline;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlightSyncConfig extends DatasyncConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(FlightSyncConfig.class);

    public static class Flag extends DatasyncConfig.Flag {
        public static String AIRLINE_LIST = "-al";
        public static String RETRY_FILE = "-rf";
    }

    public static class Defaults {
        public static int THREAD_COUNT = 3;
        public static int THREAD_COUNT_MAX = 5;
        public static SyncMode SYNC_MODE = SyncMode.FULL_SYNC;
    }

    public enum SyncMode {
        FULL_SYNC,
        AIRLINE_SYNC,
        RETRY_SYNC
    }

    public FlightSyncConfig(String[] args) {
        super(args);
        validateGeoNamesUser();
        processThreadCount();
        processSyncMode();
        if (this.additionalOptions.get(Flag.SYNC_MODE).equals(SyncMode.AIRLINE_SYNC)) {
            processAirlineList();
        }
        if (this.additionalOptions.get(Flag.SYNC_MODE).equals(SyncMode.RETRY_SYNC)) {
            processRetryFile();
        }
    }

    private void processRetryFile() {
        if (!this.additionalOptions.containsKey(Flag.RETRY_FILE)) {
            throw new RuntimeException(DatasyncConfig.Messages.getMissingMessage("retry file",
                    Flag.RETRY_FILE));
        }
        String file = (String) this.additionalOptions.get(Flag.RETRY_FILE);
        try (InputStream is = new FileInputStream(file)) {
            LOGGER.info("successfully loaded retry file: {}",file);
        } catch (IOException e) {
            throw new RuntimeException(DatasyncConfig.Messages.getInvalidValueMessage("retry file",
                    Flag.RETRY_FILE,"filename",file));
        }
    }

    private void processAirlineList() {
        if (!this.additionalOptions.containsKey(Flag.AIRLINE_LIST)) {
            throw new RuntimeException(DatasyncConfig.Messages.getMissingMessage("airline list",
                    Flag.AIRLINE_LIST));
        }
        String listString = (String) this.additionalOptions.get(Flag.AIRLINE_LIST);
        String[] tokens = listString.split(",");
        if (tokens.length == 0) {
            throw new RuntimeException(DatasyncConfig.Messages.getEmptyListConstraintElems("airline list",
                    Flag.AIRLINE_LIST,"is a valid airline enum"));
        }
        List<Airline> airlineList = new ArrayList<>();
        for (String token : tokens) {
            try {
                airlineList.add(Airline.valueOf(token.toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException(DatasyncConfig.Messages.getInvalidListMessage("airline list",
                        Flag.AIRLINE_LIST, Arrays.stream(Airline.values()).map(Airline::name).toList()));
            }
        }
        this.additionalOptions.put(Flag.AIRLINE_LIST,airlineList);
    }

    private void processThreadCount() {
        int fromProgramArgs = (int) this.optionMap.get(Flag.THREAD_COUNT);
        if (fromProgramArgs == DatasyncConfig.Defaults.THREAD_COUNT) {
            this.optionMap.put(Flag.THREAD_COUNT,Defaults.THREAD_COUNT);
        } else if (fromProgramArgs > Defaults.THREAD_COUNT_MAX) {
            LOGGER.info("Provided thread count {} exceeds maximum. Setting thread count to maximum: {}", fromProgramArgs, Defaults.THREAD_COUNT_MAX);
            this.optionMap.put(Flag.THREAD_COUNT,Defaults.THREAD_COUNT_MAX);
        }
    }

    private void processSyncMode() {
        if (this.additionalOptions.containsKey(DatasyncConfig.Flag.SYNC_MODE)) {
            String syncModeString = (String) this.additionalOptions.get(DatasyncConfig.Flag.SYNC_MODE);
            if (StringUtils.isBlank(syncModeString)) {
                throw new IllegalArgumentException(DatasyncConfig.Messages.getBlankValueMessage(
                        "sync mode",DatasyncConfig.Flag.SYNC_MODE));
            }
            try {
                this.additionalOptions.put(DatasyncConfig.Flag.SYNC_MODE,SyncMode.valueOf(syncModeString));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException(DatasyncConfig.Messages.getInvaliValueListValidMessage(
                        "sync mode",DatasyncConfig.Flag.SYNC_MODE,syncModeString,Arrays.stream(SyncMode.values())
                                .map(SyncMode::name).toList()));
            }
        } else {
            this.additionalOptions.put(DatasyncConfig.Flag.SYNC_MODE,Defaults.SYNC_MODE);
        }
    }

    public String getFileName() {
        return (String) this.additionalOptions.get(Flag.RETRY_FILE);
    }

    public List<Airline> getAirlineList() {
        return (List<Airline>) this.additionalOptions.get(Flag.AIRLINE_LIST);
    }

    public SyncMode getSyncMode() {
        return (SyncMode) this.additionalOptions.get(DatasyncConfig.Flag.SYNC_MODE);
    }
}
