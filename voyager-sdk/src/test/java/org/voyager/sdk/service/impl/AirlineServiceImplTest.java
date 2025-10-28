package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.sdk.model.AirlineQuery;
import org.voyager.commons.model.airline.AirlineAirport;
import org.voyager.commons.model.airline.AirlineBatchUpsert;
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
    void init() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerTestImplementation(
                AirlineService.class, AirlineServiceImpl.class,ServiceUtilsTestFactory.getInstance());
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

        either = airlineService.getAirlines(); assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(Airline.DELTA,either.get().get(0));
    }

    @Test
    void batchUpsert() {
        assertThrows(NullPointerException.class,() -> airlineService.batchUpsert(null));
        AirlineBatchUpsert airlineBatchUpsert = AirlineBatchUpsert.builder().iataList(List.of("HEL")).build();
        Either<ServiceError, List<AirlineAirport>> either = airlineService.batchUpsert(airlineBatchUpsert);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(Airline.JAPAN,either.get().get(0).getAirline());
    }

    @Test
    void batchDeleteAirline() {
        assertThrows(NullPointerException.class,() -> airlineService.batchDeleteAirline(null));
        Either<ServiceError, Integer> either = airlineService.batchDeleteAirline(Airline.EMIRATES);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(1,either.get());
    }
}