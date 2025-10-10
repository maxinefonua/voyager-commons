package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;
import org.voyager.model.LocationQuery;
import org.voyager.model.location.Location;
import org.voyager.model.location.LocationForm;
import org.voyager.model.location.LocationPatch;
import org.voyager.model.location.Source;
import org.voyager.service.LocationService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import org.voyager.utils.ServiceUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LocationSerivceImplTest {
    private static LocationService locationService;

    @BeforeAll
    static void setUp() {
        TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
        testServiceRegistry.registerSupplier(LocationService.class,() -> {
            try {
                return LocationSerivceImpl.class.getDeclaredConstructor(ServiceUtils.class)
                        .newInstance(ServiceUtilsTestFactory.getInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        locationService = testServiceRegistry.get(LocationService.class);
        assertNotNull(locationService);
        assertInstanceOf(LocationSerivceImpl.class,locationService);
    }

    @Test
    void getLocations() {
        Either<ServiceError, List<Location>> either = locationService.getLocations();
        assertNotNull(either);
        assertTrue(either.isRight());
        assertFalse(either.get().isEmpty());
        assertNotNull(either.get().get(0));

        either = locationService.getLocations(LocationQuery.builder().withLimit(20).build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertFalse(either.get().isEmpty());
        assertNotNull(either.get().get(0));
    }

    @Test
    void getLocation() {
        assertThrows(NullPointerException.class,()->locationService.getLocation(null));
        Either<ServiceError, Location> either = locationService.getLocation(2);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals(2,either.get().getId());
    }

    @Test
    void testGetLocation() {
        assertThrows(NullPointerException.class,()->locationService.getLocation(null,null));
        assertThrows(NullPointerException.class,()->locationService.getLocation(Source.MANUAL,null));
        Either<ServiceError, Location> either = locationService.getLocation(Source.MANUAL,"test-source-id");
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals(Source.MANUAL,either.get().getSource());
        assertEquals("test-source-id",either.get().getSourceId());
    }

    @Test
    void deleteLocation() {
        assertThrows(NullPointerException.class,()->locationService.deleteLocation(null));
        Either<ServiceError,Void> either = locationService.deleteLocation(2);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNull(either.get());
    }

    @Test
    void createLocation() {
        assertThrows(NullPointerException.class,()->locationService.createLocation(null));
        Either<ServiceError, Location> either = locationService.createLocation(LocationForm.builder().build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals("test location",either.get().getName());
    }

    @Test
    void patchLocation() {
        assertThrows(NullPointerException.class,()->locationService.patchLocation(null,null));
        assertThrows(NullPointerException.class,()->locationService.patchLocation(2,null));
        Either<ServiceError, Location> either = locationService.patchLocation(2, LocationPatch.builder().build());
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertEquals("test location",either.get().getName());
        assertEquals(2,either.get().getId());
    }
}