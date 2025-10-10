package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;
import org.voyager.model.Airline;
import org.voyager.model.PathAirlineQuery;
import org.voyager.model.PathQuery;
import org.voyager.model.route.AirlinePath;
import org.voyager.model.route.PathResponse;
import org.voyager.model.route.RoutePath;
import org.voyager.service.PathService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import org.voyager.utils.ServiceUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathServiceImplTest {
    private static PathService pathService;

    @BeforeAll
    static void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerSupplier(PathService.class,()->{
            try {
                return PathServiceImpl.class.getDeclaredConstructor(ServiceUtils.class)
                        .newInstance(ServiceUtilsTestFactory.getInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        pathService = testServiceRegistry.get(PathService.class);
        assertNotNull(pathService);
        assertInstanceOf(PathServiceImpl.class,pathService);
    }

    @Test
    void getAirlinePathResponse() {
        assertThrows(NullPointerException.class, ()->pathService.getAirlinePathResponse(null));
        Either<ServiceError, PathResponse<AirlinePath>> either = pathService.getAirlinePathResponse(
                PathAirlineQuery.builder().withOriginIATAList(List.of("SJC"))
                        .withDestinationIATAList(List.of("SLC")).build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertNotNull(either.get().getAirlines());
        assertFalse(either.get().getResponseList().isEmpty());
        assertInstanceOf(AirlinePath.class,either.get().getResponseList().get(0));
        assertEquals(Airline.DELTA,either.get().getResponseList().get(0).getAirline());
    }

    @Test
    void getRoutePathList() {
        assertThrows(NullPointerException.class, ()->pathService.getRoutePathList(null));
        Either<ServiceError, List<RoutePath>> either = pathService.getRoutePathList(
                PathQuery.builder().withOriginIATAList(List.of("SJC"))
                        .withDestinationIATAList(List.of("SLC")).build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertFalse(either.get().isEmpty());
        assertNotNull(either.get().get(0));
        assertNotNull(either.get().get(0).getRouteAirlineList());
        assertFalse(either.get().get(0).getRouteAirlineList().isEmpty());
        assertNotNull(either.get().get(0).getRouteAirlineList().get(0));
        assertNotNull(either.get().get(0).getRouteAirlineList().get(0).getAirlines());
        assertFalse(either.get().get(0).getRouteAirlineList().get(0).getAirlines().isEmpty());
        assertEquals(Airline.UNITED,either.get().get(0).getRouteAirlineList().get(0).getAirlines().get(1));
    }
}