package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.*;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.airport.Airport;
import org.voyager.model.airport.AirportPatch;
import org.voyager.model.airport.AirportType;
import org.voyager.model.AirportQuery;
import org.voyager.model.NearbyAirportQuery;
import org.voyager.service.AirportService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import org.voyager.utils.ServiceUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirportServiceImplTest {
    private static AirportService airportService;
    @BeforeAll
    static void init() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerSupplier(AirportService.class,() -> {
            try {
                return AirportServiceImpl.class.getDeclaredConstructor(ServiceUtils.class)
                        .newInstance(ServiceUtilsTestFactory.getInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        airportService = testServiceRegistry.get(AirportService.class);
        assertNotNull(airportService);
    }

    @Test
    void getAirports() {
        assertThrows(NullPointerException.class,()->airportService.getAirports(null));

        Either<ServiceError, List<Airport>> either = airportService.getAirports(AirportQuery.builder().withAirline(Airline.DELTA)
                .withCountryCode("TO").withTypeList(List.of(AirportType.CIVIL)).build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertFalse(either.get().isEmpty());
        assertEquals("IATA",either.get().get(0).getIata());

        either = airportService.getAirports();
        assertNotNull(either);
        assertTrue(either.isRight());
    }

    @Test
    void getAirport() {
        assertThrows(NullPointerException.class,()->airportService.getAirport(null));
        Either<ServiceError,Airport> either = airportService.getAirport("IATA");
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals("IATA",either.get().getIata());
    }

    @Test
    void patchAirport() {
        assertThrows(NullPointerException.class,()->airportService.patchAirport(null,null));
        assertThrows(NullPointerException.class,()->airportService.patchAirport("IATA",null));
        Either<ServiceError,Airport> either = airportService.patchAirport("IATA",
                AirportPatch.builder().type(AirportType.MILITARY.name()).build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(AirportType.MILITARY,either.get().getType());
    }

    @Test
    void getIATACodes() {
        assertThrows(NullPointerException.class,()->airportService.getIATACodes(null));
        Either<ServiceError,List<String>> either = airportService.getIATACodes(List.of(AirportType.HISTORICAL));
        assertNotNull(either);
        assertTrue(either.isRight());
        assertFalse(either.get().isEmpty());
        assertEquals("IATA",either.get().get(0));
    }

    @Test
    void getNearbyAirports() {
        assertThrows(NullPointerException.class,()->airportService.getNearbyAirports(null));
        NearbyAirportQuery nearbyAirportQuery = NearbyAirportQuery.builder().withLatitude(1.0)
                .withLongitude(-1.0).withLimit(3).withAirlineList(List.of(Airline.AIRNZ))
                .withAirportTypeList(List.of(AirportType.UNVERIFIED)).build();
        Either<ServiceError, List<Airport>> either = airportService.getNearbyAirports(nearbyAirportQuery);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertFalse(either.get().isEmpty());
        assertEquals(AirportType.UNVERIFIED,either.get().get(0).getType());
    }
}