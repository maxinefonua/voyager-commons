package org.voyager.utils;

import org.voyager.config.VoyagerConfig;

public class ServiceUtilsFactory {
    private static ServiceUtils DEFAULT_INSTANCE;

    public static void initialize(VoyagerConfig voyagerConfig){
        if (DEFAULT_INSTANCE != null)
            throw new IllegalStateException("ServiceUtilsFactory already initialized");
        DEFAULT_INSTANCE = new ServiceUtilsDefault(voyagerConfig);
    }

    public static ServiceUtils getInstance() {
        if (DEFAULT_INSTANCE == null)
            throw new IllegalStateException("ServiceUtilsFactory not yet initialized. Call initialize() first");
        return DEFAULT_INSTANCE;
    }
}
