package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.route.*;
import org.voyager.sdk.service.RouteService;
import org.voyager.sdk.service.RouteSyncService;
import org.voyager.sdk.service.TestServiceRegistry;
import org.voyager.sdk.service.utils.ServiceUtilsTestFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RouteSyncServiceImplTest {
    private static RouteSyncService routeSyncService;

    @BeforeAll
    static void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerTestImplementation(
                RouteSyncService.class, RouteSyncServiceImpl.class, ServiceUtilsTestFactory.getInstance());
        routeSyncService = testServiceRegistry.get(RouteSyncService.class);
        assertNotNull(routeSyncService);
        assertInstanceOf(RouteSyncServiceImpl.class,routeSyncService);
    }

    @Test
    void getByStatus() {
        assertThrows(NullPointerException.class,()->routeSyncService.getByStatus(null));
        Either<ServiceError, List<RouteSync>> either = routeSyncService.getByStatus(Status.PENDING);
        assertNotNull(either);
        assertTrue(either.isRight());
    }

    @Test
    void batchUpdate() {
        assertThrows(NullPointerException.class,()->routeSyncService.batchUpdate(null));
        RouteSyncBatchUpdate routeSyncBatchUpdate = RouteSyncBatchUpdate.builder()
                .routeIdList(List.of(1,2))
                .status(Status.PROCESSING)
                .build();
        Either<ServiceError, Integer> either = routeSyncService.batchUpdate(routeSyncBatchUpdate);
        assertNotNull(either);
        assertTrue(either.isRight());
    }

    @Test
    void patchRouteSync() {
        assertThrows(NullPointerException.class,()->routeSyncService.patchRouteSync(null,null));
        assertThrows(NullPointerException.class,()->routeSyncService.patchRouteSync(1,null));
        RouteSyncPatch routeSyncPatch = RouteSyncPatch.builder()
                .status(Status.COMPLETED)
                .build();
        Either<ServiceError, RouteSync> either = routeSyncService.patchRouteSync(1,routeSyncPatch);
        assertNotNull(either);
        assertTrue(either.isRight());
    }
}