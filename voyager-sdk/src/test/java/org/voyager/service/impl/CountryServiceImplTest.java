package org.voyager.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;
import org.voyager.model.CountryQuery;
import org.voyager.model.country.Continent;
import org.voyager.model.country.Country;
import org.voyager.model.country.CountryForm;
import org.voyager.service.AirlineService;
import org.voyager.service.CountryService;
import org.voyager.service.TestServiceRegistry;
import org.voyager.service.utils.ServiceUtilsTestFactory;
import org.voyager.utils.ServiceUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CountryServiceImplTest {
    private static TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
    private static CountryService countryService;

    @BeforeEach
    void setUp() {
        testServiceRegistry.registerSupplier(CountryService.class,() ->{
            try {
                return CountryServiceImpl.class.getDeclaredConstructor(ServiceUtils.class)
                        .newInstance(ServiceUtilsTestFactory.getInstance());
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
        countryService = testServiceRegistry.get(CountryService.class);
    }

    @AfterEach
    void tearDown() {
        testServiceRegistry.reset();
    }

    @Test
    void testConstructor() {
        assertNotNull(testServiceRegistry);
        testServiceRegistry.registerImplementation(CountryService.class,CountryServiceImpl.class);
        CountryService actualCountryService = testServiceRegistry.get(CountryService.class);
        assertNotNull(actualCountryService);
        assertInstanceOf(CountryServiceImpl.class,actualCountryService);
        assertNotNull(countryService);
    }

    @Test
    void getCountries() {
        Either<ServiceError, List<Country>> either = countryService.getCountries(null);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertFalse(either.get().isEmpty());
        assertEquals("TS",either.get().get(0).getCode());
    }

    @Test
    void getCountry() {
        Either<ServiceError, Country> either = countryService.getCountry("TS");
        assertNotNull(either);
        assertTrue(either.isRight());
        assertEquals("TS",either.get().getCode());

    }

    @Test
    void addCountry() {
        assertThrows(NullPointerException.class,() -> countryService.addCountry(null));
        CountryForm countryForm = CountryForm.builder().build();
        Either<ServiceError, Country> either = countryService.addCountry(countryForm);
        assertNotNull(either);
        assertTrue(either.isRight());
        assertNotNull(either.get());
        assertInstanceOf(Country.class,either.get());
    }
}