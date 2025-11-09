package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.path.airline.PathAirlineQuery;
import org.voyager.sdk.model.RoutePathQuery;
import org.voyager.commons.model.path.airline.AirlinePath;
import org.voyager.commons.model.path.PathResponse;
import org.voyager.commons.model.path.route.RoutePath;
import org.voyager.sdk.service.PathService;
import org.voyager.sdk.service.TestServiceRegistry;
import org.voyager.sdk.service.utils.ServiceUtilsTestFactory;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PathServiceImplTest {
    private static PathService pathService;

    @BeforeAll
    static void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerTestImplementation(
                PathService.class, PathServiceImpl.class,ServiceUtilsTestFactory.getInstance());
        pathService = testServiceRegistry.get(PathService.class);
        assertNotNull(pathService);
        assertInstanceOf(PathServiceImpl.class,pathService);
    }

    @Test
    void getAirlinePathResponse() {
        assertThrows(IllegalArgumentException.class, ()->pathService.getAirlinePathResponse(null));
        Either<ServiceError, PathResponse<AirlinePath>> either = pathService.getAirlinePathResponse(
                PathAirlineQuery.builder().originSet(Set.of("SJC"))
                        .destinationSet(Set.of("SLC")).build());
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
                RoutePathQuery.builder().withOriginIATAList(List.of("SJC"))
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