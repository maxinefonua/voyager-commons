package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.route.RouteQuery;
import org.voyager.commons.model.route.Route;
import org.voyager.commons.model.route.RouteForm;
import org.voyager.commons.model.route.RoutePatch;
import org.voyager.sdk.service.RouteService;
import org.voyager.sdk.service.TestServiceRegistry;
import org.voyager.sdk.service.utils.ServiceUtilsTestFactory;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class RouteServiceImplTest {
    private static RouteService routeService;

    @BeforeAll
    static void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerTestImplementation(
                RouteService.class, RouteServiceImpl.class,ServiceUtilsTestFactory.getInstance());
        routeService = testServiceRegistry.get(RouteService.class);
        assertNotNull(routeService);
        assertInstanceOf(RouteServiceImpl.class,routeService);
    }

    @Test
    void getRoutes() {
        Either<ServiceError, List<Route>> either = routeService.getRoutes(RouteQuery.builder().originList(List.of("SJC")).build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertFalse(either.get().isEmpty());
        assertNotNull(either.get().get(0));

        either = routeService.getRoutes();
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertFalse(either.get().isEmpty());
        assertNotNull(either.get().get(0));
    }

    @Test
    void getRoute() {
        assertThrows(NullPointerException.class,()->routeService.getRoute(null));
        Either<ServiceError, Route> either = routeService.getRoute(555);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals(555,either.get().getId());
    }

    @Test
    void testGetRoute() {
        assertThrows(NullPointerException.class,()->routeService.getRoute(null,null));
        assertThrows(NullPointerException.class,()->routeService.getRoute("HNL",null));
        Either<ServiceError, Route> either = routeService.getRoute("HNL","HND");
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals("HNL",either.get().getOrigin());
        assertEquals("HND",either.get().getDestination());
    }

    @Test
    void createRoute() {
        assertThrows(NullPointerException.class,()->routeService.createRoute(null));
        Either<ServiceError, Route> either = routeService.createRoute(RouteForm.builder().build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals(555,either.get().getId());
    }

    @Test
    void patchRoute() {
        assertThrows(NullPointerException.class,()->routeService.patchRoute(null,null));
        assertThrows(NullPointerException.class,()->routeService.patchRoute(555,null));
        Either<ServiceError, Route> either = routeService.patchRoute(555, RoutePatch.builder().build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals(555,either.get().getId());
    }
}