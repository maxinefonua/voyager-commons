package org.voyager.config;

import org.junit.platform.commons.util.StringUtils;

public class CountriesSyncConfig extends DatasyncConfig{

    public CountriesSyncConfig(String[] args) {
        super(args);
        validateGeoNamesUser();
    }
}
