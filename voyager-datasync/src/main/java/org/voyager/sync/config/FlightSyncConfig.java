package org.voyager.sync.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.voyager.commons.model.airline.Airline;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FlightSyncConfig extends DatasyncConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightSyncConfig.class);

    public static class Flag extends DatasyncConfig.Flag {
        public static String RETRY_ROUTE_FILE = "-rr";
        public static String RETRY_AIRLINE_FILE = "-ra";
        public static String RETENTION_DAYS = "-rd";
    }

    public static class Defaults {
        public static int THREAD_COUNT = 3;
        public static int THREAD_COUNT_MAX = 5;
        public static int RETENTION_DAYS = 2;
        public static int RETENTION_DAYS_MIN = 2;
    }

    public FlightSyncConfig(String[] args) {
        super(args);
        validateGeoNamesUser();
        processThreadCount();
        processRetryFiles();
        processRetentionDays();
    }

    private void processRetentionDays() {
        if (!this.additionalOptions.containsKey(Flag.RETENTION_DAYS)) {
            this.additionalOptions.put(Flag.RETENTION_DAYS,Defaults.RETENTION_DAYS);
        } else {
            String retentionString = (String) this.additionalOptions.get(Flag.RETENTION_DAYS);
            try {
                int retentionDays = Integer.parseInt(retentionString);
                if (retentionDays < Defaults.RETENTION_DAYS_MIN) {
                    throw new RuntimeException(DatasyncConfig.Messages.getInvalidValueMessage("retention days",
                            Flag.RETENTION_DAYS, "integer at minimum value of 3", retentionString));
                }
                this.additionalOptions.put(Flag.RETENTION_DAYS, retentionDays);
            } catch (NumberFormatException e) {
                throw new RuntimeException(DatasyncConfig.Messages.getInvalidValueMessage("retention days",
                        Flag.RETENTION_DAYS, "integer", retentionString));
            }
        }
    }

    private void processRetryFiles() {
        if (!this.additionalOptions.containsKey(Flag.RETRY_ROUTE_FILE)) {
            throw new RuntimeException(DatasyncConfig.Messages.getMissingMessage("retry route file",
                    Flag.RETRY_ROUTE_FILE));
        }
        if (!this.additionalOptions.containsKey(Flag.RETRY_AIRLINE_FILE)) {
            throw new RuntimeException(DatasyncConfig.Messages.getMissingMessage("retry airline file",
                    Flag.RETRY_AIRLINE_FILE));
        }
        String retryRouteFile = (String) this.additionalOptions.get(Flag.RETRY_ROUTE_FILE);
        try (InputStream ignored = new FileInputStream(retryRouteFile)) {
            LOGGER.info("successfully loaded retry route file: {}",retryRouteFile);
        } catch (IOException e) {
            throw new RuntimeException(DatasyncConfig.Messages.getInvalidValueMessage("retry route file",
                    Flag.RETRY_ROUTE_FILE,"filename",retryRouteFile));
        }
        String retryAirlineFile = (String) this.additionalOptions.get(Flag.RETRY_AIRLINE_FILE);
        try (FileWriter fileWriter = new FileWriter(retryAirlineFile)) {
            this.additionalOptions.put(Flag.RETRY_AIRLINE_FILE,fileWriter);
            LOGGER.info("successfully loaded retry airline file: {}",retryAirlineFile);
        } catch (IOException e) {
            throw new RuntimeException(DatasyncConfig.Messages.getInvalidValueMessage("retry airline file",
                    Flag.RETRY_AIRLINE_FILE,"filename",retryAirlineFile));
        }
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

    public String getRetryRouteFileName() {
        return (String) this.additionalOptions.get(Flag.RETRY_ROUTE_FILE);
    }
    public FileWriter getRetryAirlineFileWriter() {
        return (FileWriter) this.additionalOptions.get(Flag.RETRY_AIRLINE_FILE);
    }

    public int getRetentionDays() {
        return (int) this.additionalOptions.get(Flag.RETENTION_DAYS);
    }
}
