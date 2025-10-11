package org.voyager.service;

import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.service.impl.VoyagerServiceRegistry;

public class TestServiceRegistry extends VoyagerServiceRegistry {
    static final VoyagerConfig VOYAGER_CONFIG = new VoyagerConfig(Protocol.HTTP,"testhost",8080,"test-token");
    private static final TestServiceRegistry INSTANCE = new TestServiceRegistry();

    public static TestServiceRegistry getInstance() {
        return INSTANCE;
    }
}
