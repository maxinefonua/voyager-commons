package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.flight.*;
import org.voyager.sdk.service.FlightService;
import org.voyager.sdk.service.TestServiceRegistry;
import org.voyager.sdk.service.utils.ServiceUtilsTestFactory;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class FlightServiceImplTest {
    static FlightService flightService;

    @BeforeAll
    static void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        assertNotNull(testServiceRegistry);
        testServiceRegistry.registerTestImplementation(
                FlightService.class, FlightServiceImpl.class,ServiceUtilsTestFactory.getInstance());

        flightService = testServiceRegistry.get(FlightService.class);
    }

    @AfterAll
    static void cleanup() {
        TestServiceRegistry.getInstance().reset();
    }

    @Test
    void testConstructor() {
        assertNotNull(flightService);
        assertInstanceOf(FlightServiceImpl.class,flightService);
    }

    @Test
    void getFlights() {
        Either<ServiceError, List<Flight>> either = flightService.getFlights();
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertFalse(either.get().isEmpty());

        either = flightService.getFlights(FlightNumberQuery.builder().flightNumber("DL100")
                .startTime(ZonedDateTime.parse("2025-11-14T16:17:34.035784-08:00[America/Los_Angeles]"))
                .endTime(ZonedDateTime.parse("2025-11-15T16:17:34.036843-08:00[America/Los_Angeles]"))
                .build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertFalse(either.get().isEmpty());
    }

    @Test
    void getFlight() {
        assertThrows(NullPointerException.class,() -> flightService.getFlight(null));
        Either<ServiceError, Flight> either = flightService.getFlight(125);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals(125,either.get().getId());
    }

    @Test
    void testGetFlight() {
        assertThrows(NullPointerException.class,() -> flightService.getFlightOnDate(
                null,null,null,null));
        assertThrows(NullPointerException.class,() -> flightService.getFlightOnDate(
                101,null,null,null));
        assertThrows(NullPointerException.class,() -> flightService.getFlightOnDate(
                101,"DL988",null,null));

        Either<ServiceError, Flight> either = flightService.getFlightOnDate(101,"DL988",
                LocalDate.parse("2025-11-13"), ZoneId.of("America/Los_Angeles"));
        assertNotNull(either);
        assertTrue(either.isRight());
    }

    @Test
    void createFlight() {
        assertThrows(NullPointerException.class,()->flightService.createFlight(null));
        Either<ServiceError, Flight> either = flightService.createFlight(FlightForm.builder().build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals("DL988",either.get().getFlightNumber());
    }

    @Test
    void testBatchDelete() {
        assertThrows(NullPointerException.class,()->flightService.batchDelete(null));
        assertThrows(ValidationException.class,()->
        flightService.batchDelete(FlightBatchDelete.builder().build()));

        Either<ServiceError, Integer> either = flightService.batchDelete(FlightBatchDelete.builder()
                .airline(Airline.FRONTIER.name()).build());
        assertTrue(either.isRight());
    }

    @Test
    void testBatchUpsert() {
        assertThrows(NullPointerException.class,()->flightService.batchUpsert(null));
        assertThrows(ValidationException.class,()->
                flightService.batchUpsert(FlightBatchUpsert.builder().build()));

        Either<ServiceError, FlightBatchUpsertResult> either = flightService.batchUpsert(FlightBatchUpsert.builder()
                .flightUpsertList(List.of(FlightUpsert.builder().flightNumber("DL123").airline(Airline.DELTA.name())
                                .isArrival("true").routeId("1").zonedDateTimeList(List.of(ZonedDateTime.now()))
                        .build())).build());
        assertTrue(either.isRight());
    }
}