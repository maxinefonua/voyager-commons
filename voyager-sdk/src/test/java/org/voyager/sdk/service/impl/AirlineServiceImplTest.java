package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.*;
import org.voyager.sdk.service.AirlineService;
import org.voyager.sdk.service.TestServiceRegistry;
import org.voyager.sdk.service.utils.ServiceUtilsTestFactory;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AirlineServiceImplTest {
    private static AirlineService airlineService;
    @BeforeEach
    void setup() {
        TestServiceRegistry.getInstance().registerTestImplementation(AirlineService.class,
                AirlineServiceImpl.class, ServiceUtilsTestFactory.getInstance());
        airlineService = VoyagerServiceRegistry.getInstance().get(AirlineService.class);
    }

    @AfterAll
    static void cleanup() {
        TestServiceRegistry.getInstance().reset();
    }

    @Test
    void getAirlines() {
        assertThrows(NullPointerException.class,() -> airlineService.getAirlines(null));
        AirlinePathQuery airlinePathQuery = AirlinePathQuery.builder()
                .originList(List.of("SJC"))
                .destinationList(List.of("ITM"))
                .build();
        Either<ServiceError, List<Airline>> either = airlineService.getAirlines(airlinePathQuery);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(Airline.DELTA,either.get().get(0));

        either = airlineService.getAirlines(); assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(Airline.DELTA,either.get().get(0));
    }
}