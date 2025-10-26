package org.voyager.sdk.service.utils;

import org.voyager.sdk.config.Protocol;
import org.voyager.sdk.config.VoyagerConfig;
import org.voyager.sdk.utils.ServiceUtils;
import org.voyager.sdk.utils.ServiceUtilsFactory;

public class ServiceUtilsTestFactory extends ServiceUtilsFactory {
    private static final VoyagerConfig VOYAGER_CONFIG = new VoyagerConfig(Protocol.HTTP,
            "testhost",8080,"test-token");
    private static ServiceUtils TEST_INSTANCE;
    public static ServiceUtils getInstance() {
        if (TEST_INSTANCE == null) TEST_INSTANCE = new TestServiceUtils(VOYAGER_CONFIG);
        return TEST_INSTANCE;
    }
}
