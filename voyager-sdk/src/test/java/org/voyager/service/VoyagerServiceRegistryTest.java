package org.voyager.service;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.utils.ServiceUtils;
import org.voyager.utils.ServiceUtilsFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VoyagerServiceRegistryTest {
    private static final VoyagerConfig VOYAGER_CONFIG = new VoyagerConfig(Protocol.HTTP,"testhost",
            8080,1,"test-token");
    private static final VoyagerServiceRegistry testRegistry = VoyagerServiceRegistry.getInstance();

    @Test
    void getInstance() {
        assertNotNull(testRegistry);
    }

    @AfterAll
    static void cleanup() {
        testRegistry.reset();
    }

    @Test
    void register() {
        assertDoesNotThrow(()->
                testRegistry.registerImplementation(AirportService.class, AirportServiceImpl.class));

        class TestClassNoConstructor {}
        assertThrows(RuntimeException.class,()->
                testRegistry.registerImplementation(Object.class,TestClassNoConstructor.class));
    }

    @Test
    void registerSupplier() {
        testRegistry.registerSupplier(Map.class,HashMap::new);
    }

    @Test
    void get() {
        assertThrows(IllegalStateException.class,()->
                testRegistry.get(String.class));
        testRegistry.registerImplementation(AirportService.class, AirportServiceImpl.class);
        assertDoesNotThrow(()->testRegistry.get(AirportService.class));
    }
}