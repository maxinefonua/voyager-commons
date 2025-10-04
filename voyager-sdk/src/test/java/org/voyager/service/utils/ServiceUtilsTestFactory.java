package org.voyager.service.utils;

import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

public class ServiceUtilsTestFactory extends ServiceUtilsFactory {
    private static ServiceUtils TEST_INSTANCE;
    public static ServiceUtils getInstance() {
        if (TEST_INSTANCE == null) TEST_INSTANCE = new TestServiceUtils();
        return TEST_INSTANCE;
    }
}
