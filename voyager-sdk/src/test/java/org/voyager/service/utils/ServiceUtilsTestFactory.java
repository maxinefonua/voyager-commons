package org.voyager.service.utils;

import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

public class ServiceUtilsTestFactory extends ServiceUtilsFactory {
    private static final VoyagerConfig VOYAGER_CONFIG = new VoyagerConfig(Protocol.HTTP,
            "testhost",8080,"test-token");
    private static ServiceUtils TEST_INSTANCE;
    public static ServiceUtils getInstance() {
        if (TEST_INSTANCE == null) TEST_INSTANCE = new TestServiceUtils(VOYAGER_CONFIG);
        return TEST_INSTANCE;
    }
}
