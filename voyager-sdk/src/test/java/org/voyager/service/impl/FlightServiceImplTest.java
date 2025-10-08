package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;
import org.voyager.model.FlightQuery;
import org.voyager.model.flight.Flight;
import org.voyager.model.flight.FlightForm;
import org.voyager.model.flight.FlightPatch;
import org.voyager.service.FlightService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import org.voyager.utils.ServiceUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FlightServiceImplTest {
    static FlightService flightService;

    @BeforeAll
    static void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        assertNotNull(testServiceRegistry);
        testServiceRegistry.registerSupplier(FlightService.class,() -> {
            try {
                return FlightServiceImpl.class.getDeclaredConstructor(ServiceUtils.class)
                        .newInstance(ServiceUtilsTestFactory.getInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });

        flightService = testServiceRegistry.get(FlightService.class);
    }

    @Test
    void testConstructor() {
        assertNotNull(flightService);
        assertInstanceOf(FlightServiceImpl.class,flightService);
    }

    @Test
    void getFlights() {
        Either<ServiceError, List<Flight>> either = flightService.getFlights(null);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertFalse(either.get().isEmpty());

        either = flightService.getFlights(FlightQuery.builder().withFlightNumber("DL100").build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertFalse(either.get().isEmpty());
        assertEquals("DL100",either.get().get(0).getFlightNumber());
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
        assertThrows(NullPointerException.class,() -> flightService.getFlight(null,null));
        Either<ServiceError, Flight> either = flightService.getFlight(101,"DL988");
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals("DL988",either.get().getFlightNumber());
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
    void patchFlight() {
        assertThrows(NullPointerException.class,()->flightService.patchFlight(null,null));
        assertThrows(NullPointerException.class,()->flightService.patchFlight(30,null));
        Either<ServiceError, Flight> either = flightService.patchFlight(30,FlightPatch.builder().build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
    }
}