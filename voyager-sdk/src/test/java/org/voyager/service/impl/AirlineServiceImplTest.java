package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;
import org.voyager.model.airline.Airline;
import org.voyager.model.AirlineQuery;
import org.voyager.service.AirlineService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirlineServiceImplTest {
    private static AirlineService airlineService;

    @BeforeEach
    void init() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerTestImplementation(
                AirlineService.class,AirlineServiceImpl.class,ServiceUtilsTestFactory.getInstance());
        airlineService = testServiceRegistry.get(AirlineService.class);
        assertNotNull(airlineService);
    }

    @Test
    void getAirlines() {
        assertThrows(NullPointerException.class,() -> airlineService.getAirlines(null));
        AirlineQuery airlineQuery = AirlineQuery.builder().withIATAList(List.of("HEL")).build();
        Either<ServiceError, List<Airline>> either = airlineService.getAirlines(airlineQuery);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(Airline.DELTA,either.get().get(0));
    }
}