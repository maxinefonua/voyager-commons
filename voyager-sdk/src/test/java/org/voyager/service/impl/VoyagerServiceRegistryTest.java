package org.voyager.service.impl;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.service.AirlineService;
import org.voyager.service.AirportService;
import org.voyager.service.CountryService;
import org.voyager.service.FlightService;
import org.voyager.service.LocationService;
import org.voyager.service.PathService;
import org.voyager.service.RouteService;
import org.voyager.service.SearchService;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import org.voyager.utils.ServiceUtilsFactory;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VoyagerServiceRegistryTest {
    private static final VoyagerServiceRegistry testRegistry = VoyagerServiceRegistry.getInstance();

    @Test
    void getInstance() {
        assertNotNull(testRegistry);
    }

    @BeforeAll
    static void init() {
        assertThrows(IllegalStateException.class, ServiceUtilsFactory::getInstance);
        ServiceUtilsTestFactory.initialize("http://test.org");
        assertThrows(IllegalStateException.class,()->ServiceUtilsTestFactory.initialize("fail on second"));
    }

    @AfterAll
    static void cleanup() {
        testRegistry.reset();
    }

    @Test
    void register() {
        assertDoesNotThrow(()-> {
            testRegistry.get(AirlineService.class);
            testRegistry.get(AirportService.class);
            testRegistry.get(CountryService.class);
            testRegistry.get(FlightService.class);
            testRegistry.get(LocationService.class);
            testRegistry.get(PathService.class);
            testRegistry.get(RouteService.class);
            testRegistry.get(SearchService.class);
        });
        class TestClassNoConstructor {}
        assertThrows(RuntimeException.class,()-> testRegistry.registerTestImplementation(
                Object.class,TestClassNoConstructor.class,ServiceUtilsTestFactory.getInstance()));
    }

    @Test
    void get() {
        assertThrows(IllegalStateException.class,()->
                testRegistry.get(String.class));
        testRegistry.get(AirportService.class);
        assertDoesNotThrow(()->testRegistry.get(AirportService.class));
    }
}