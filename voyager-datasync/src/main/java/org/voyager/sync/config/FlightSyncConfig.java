package org.voyager.sync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FlightSyncConfig extends DatasyncConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlightSyncConfig.class);

    public static class Flag extends DatasyncConfig.Flag {
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

    private void processThreadCount() {
        int fromProgramArgs = (int) this.optionMap.get(Flag.THREAD_COUNT);
        if (fromProgramArgs == DatasyncConfig.Defaults.THREAD_COUNT) {
            this.optionMap.put(Flag.THREAD_COUNT,Defaults.THREAD_COUNT);
        } else if (fromProgramArgs > Defaults.THREAD_COUNT_MAX) {
            LOGGER.info("Provided thread count {} exceeds maximum. Setting thread count to maximum: {}", fromProgramArgs, Defaults.THREAD_COUNT_MAX);
            this.optionMap.put(Flag.THREAD_COUNT,Defaults.THREAD_COUNT_MAX);
        }
    }

    public int getRetentionDays() {
        return (int) this.additionalOptions.get(Flag.RETENTION_DAYS);
    }
}
