package org.voyager.sync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AirlineSyncConfig extends DatasyncConfig{
    private static final Logger LOGGER = LoggerFactory.getLogger(AirlineSyncConfig.class);
    public AirlineSyncConfig(String[] args) {
        super(args);
    }
}
