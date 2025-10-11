package org.voyager.service.impl;

import org.junit.jupiter.api.Test;
import org.voyager.config.Protocol;
import org.voyager.config.VoyagerConfig;
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
    private static final VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,
            "test.org","test-token");

    @Test
    void getInstance() {
        assertThrows(IllegalStateException.class,ServiceUtilsFactory::getInstance);
        assertThrows(IllegalStateException.class,()->VoyagerServiceRegistry.getInstance().get(AirportService.class));
        voyagerConfig.setTestMode(true);
        VoyagerServiceRegistry.initialize(voyagerConfig);
        assertThrows(IllegalStateException.class,()->ServiceUtilsFactory.initialize(voyagerConfig));
        assertDoesNotThrow(()->VoyagerServiceRegistry.initialize(voyagerConfig));
        assertNotNull(VoyagerServiceRegistry.getInstance());

        assertDoesNotThrow(()-> {
            VoyagerServiceRegistry testRegistry = VoyagerServiceRegistry.getInstance();
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
        assertThrows(RuntimeException.class,()-> VoyagerServiceRegistry.getInstance().registerTestImplementation(
                Object.class,TestClassNoConstructor.class,ServiceUtilsTestFactory.getInstance()));

        VoyagerServiceRegistry testRegistry = VoyagerServiceRegistry.getInstance();
        assertThrows(IllegalStateException.class,()->
                testRegistry.get(String.class));
        testRegistry.get(AirportService.class);
        assertDoesNotThrow(()->testRegistry.get(AirportService.class));

        VoyagerServiceRegistry.getInstance().reset();
    }
}