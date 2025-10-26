package org.voyager.sync.config;

public class CountriesSyncConfig extends DatasyncConfig{

    public CountriesSyncConfig(String[] args) {
        super(args);
        validateGeoNamesUser();
    }
}
