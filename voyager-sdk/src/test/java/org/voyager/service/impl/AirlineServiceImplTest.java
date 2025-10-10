package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.AirlineQuery;
import org.voyager.service.AirlineService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import org.voyager.utils.ServiceUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirlineServiceImplTest {
    private static AirlineService airlineService;

    @BeforeAll
    static void init() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerSupplier(AirlineService.class,() ->{
            try {
                return AirlineServiceImpl.class.getDeclaredConstructor(ServiceUtils.class)
                        .newInstance(ServiceUtilsTestFactory.getInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        airlineService = testServiceRegistry.get(AirlineService.class);
        assertNotNull(airlineService);
    }

    @Test
    void getAirportAirlines() {
        assertThrows(NullPointerException.class,() -> airlineService.getAirportAirlines(null));
        AirlineQuery airlineQuery = AirlineQuery.builder().withIATAList(List.of("HEL")).build();
        Either<ServiceError, List<Airline>> either = airlineService.getAirportAirlines(airlineQuery);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals(Airline.DELTA,either.get().get(0));
    }
}