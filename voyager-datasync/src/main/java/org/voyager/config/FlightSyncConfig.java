package org.voyager.config;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

public class FlightSyncConfig extends DatasyncConfig {
    private static Logger LOGGER = LoggerFactory.getLogger(FlightSyncConfig.class);

    public static class Defaults {
        public static int THREAD_COUNT = 3;
        public static int THREAD_COUNT_MAX = 5;
        public static SyncMode SYNC_MODE = SyncMode.FULL_SYNC;
    }

    public enum SyncMode {
        FULL_SYNC,
        RETRY_SYNC
    }

    public FlightSyncConfig(String[] args) {
        super(args);
        validateGeoNamesUser();
        processThreadCount();
        processSyncMode();
    }

    private void processThreadCount() {
        int fromProgramArgs = (int) this.optionMap.get(DatasyncConfig.Flag.THREAD_COUNT);
        if (fromProgramArgs == DatasyncConfig.Defaults.THREAD_COUNT) {
            this.optionMap.put(DatasyncConfig.Flag.THREAD_COUNT,Defaults.THREAD_COUNT);
        } else if (fromProgramArgs > Defaults.THREAD_COUNT_MAX) {
            LOGGER.info("Provided thread count {} exceeds maximum. Setting thread count to maximum: {}", fromProgramArgs, Defaults.THREAD_COUNT_MAX);
            this.optionMap.put(DatasyncConfig.Flag.THREAD_COUNT,Defaults.THREAD_COUNT_MAX);
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
                throw new IllegalArgumentException(DatasyncConfig.Messages.getInvalidValueMessage(
                        "sync mode",DatasyncConfig.Flag.SYNC_MODE,syncModeString,Arrays.stream(SyncMode.values())
                                .map(SyncMode::name).toList()));
            }
        } else {
            this.additionalOptions.put(DatasyncConfig.Flag.SYNC_MODE,Defaults.SYNC_MODE);
        }
    }

    public SyncMode getSyncMode() {
        return (SyncMode) this.additionalOptions.get(DatasyncConfig.Flag.SYNC_MODE);
    }
}
