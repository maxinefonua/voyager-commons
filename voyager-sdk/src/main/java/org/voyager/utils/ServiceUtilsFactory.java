package org.voyager.utils;

public class ServiceUtilsFactory {
    private static ServiceUtils DEFAULT_INSTANCE;
    public static ServiceUtils getInstance() {
        if (DEFAULT_INSTANCE == null) DEFAULT_INSTANCE = new ServiceUtilsDefault();
        return DEFAULT_INSTANCE;
    }
}
