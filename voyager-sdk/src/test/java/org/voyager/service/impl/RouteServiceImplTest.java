package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;
import org.voyager.model.RouteQuery;
import org.voyager.model.route.Route;
import org.voyager.model.route.RouteForm;
import org.voyager.model.route.RoutePatch;
import org.voyager.service.RouteService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import org.voyager.utils.ServiceUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteServiceImplTest {
    private static RouteService routeService;

    @BeforeAll
    static void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerSupplier(RouteService.class,()->{
            try {
                return RouteServiceImpl.class.getDeclaredConstructor(ServiceUtils.class)
                        .newInstance(ServiceUtilsTestFactory.getInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        routeService = testServiceRegistry.get(RouteService.class);
        assertNotNull(routeService);
        assertInstanceOf(RouteServiceImpl.class,routeService);
    }

    @Test
    void getRoutes() {
        Either<ServiceError, List<Route>> either = routeService.getRoutes(RouteQuery.builder().withOrigin("sjc").build());
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