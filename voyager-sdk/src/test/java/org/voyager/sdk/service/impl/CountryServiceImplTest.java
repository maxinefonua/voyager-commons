package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;
import org.voyager.sdk.model.CountryQuery;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.country.Country;
import org.voyager.commons.model.country.CountryForm;
import org.voyager.sdk.service.CountryService;
import org.voyager.sdk.service.TestServiceRegistry;
import org.voyager.sdk.service.utils.ServiceUtilsTestFactory;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class CountryServiceImplTest {
    private static TestServiceRegistry testServiceRegistry = TestServiceRegistry.getInstance();
    private static CountryService countryService;

    @BeforeEach
    void setUp() {
        testServiceRegistry.registerTestImplementation(
                CountryService.class, CountryServiceImpl.class,ServiceUtilsTestFactory.getInstance());
        countryService = testServiceRegistry.get(CountryService.class);
    }

    @AfterEach
    void tearDown() {
        testServiceRegistry.reset();
    }

    @Test
    void testConstructor() {
        assertNotNull(testServiceRegistry);
        testServiceRegistry.get(CountryService.class);
        CountryService actualCountryService = testServiceRegistry.get(CountryService.class);
        assertNotNull(actualCountryService);
        assertInstanceOf(CountryServiceImpl.class,actualCountryService);
        assertNotNull(countryService);
    }

    @Test
    void getCountries() {
        Either<ServiceError, List<Country>> either = countryService.getCountries();
        assertNotNull(either);
        assertTrue(either.isRight());
        assertFalse(either.get().isEmpty());
        assertEquals("TS",either.get().get(0).getCode());

        assertThrows(NullPointerException.class,()->countryService.getCountries(null));
        CountryQuery countryQuery = CountryQuery.builder().withContinentList(List.of(Continent.OC)).build();
        either = countryService.getCountries(countryQuery);
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