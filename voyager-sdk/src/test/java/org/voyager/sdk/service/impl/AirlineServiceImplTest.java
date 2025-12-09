package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airline.AirlineAirportQuery;
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
        AirlineAirportQuery airlineAirportQuery = AirlineAirportQuery.builder().iatalist(List.of("HEL")).build();
        Either<ServiceError, List<Airline>> either = airlineService.getAirlines(airlineAirportQuery);
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