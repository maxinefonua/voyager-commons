package org.voyager.service.impl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
import org.voyager.service.*;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VoyagerServiceRegistryTest {
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
        assertDoesNotThrow(()-> {
            testRegistry.registerImplementation(AirlineService.class, AirlineServiceImpl.class);
            testRegistry.registerImplementation(AirportService.class, AirportServiceImpl.class);
            testRegistry.registerImplementation(CountryService.class, CountryServiceImpl.class);
            testRegistry.registerImplementation(FlightService.class, FlightServiceImpl.class);
            testRegistry.registerImplementation(LocationService.class, LocationSerivceImpl.class);
            testRegistry.registerImplementation(PathService.class, PathServiceImpl.class);
            testRegistry.registerImplementation(RouteService.class, RouteServiceImpl.class);
            testRegistry.registerImplementation(SearchService.class, SearchServiceImpl.class);
        });

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