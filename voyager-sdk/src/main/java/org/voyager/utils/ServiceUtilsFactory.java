package org.voyager.utils;

public class ServiceUtilsFactory {
    private static ServiceUtils DEFAULT_INSTANCE;

    public static void initialize(String baseURL){
        if (DEFAULT_INSTANCE != null)
            throw new IllegalStateException("ServiceUtilsFactory already initialized");
        DEFAULT_INSTANCE = new ServiceUtilsDefault(baseURL);
    }

    public static ServiceUtils getInstance() {
        if (DEFAULT_INSTANCE == null)
            throw new IllegalStateException("ServiceUtilsFactory not yet initialized. Call initialize() first");
        return DEFAULT_INSTANCE;
    }
}
