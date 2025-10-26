package org.voyager.sdk.service.impl;

import io.vavr.control.Either;
import org.junit.jupiter.api.*;
import org.voyager.commons.error.ServiceError;
import org.voyager.commons.model.country.Continent;
import org.voyager.commons.model.geoname.GeoCountry;
import org.voyager.commons.model.geoname.GeoFull;
import org.voyager.commons.model.geoname.GeoPlace;
import org.voyager.commons.model.geoname.GeoTimezone;
import org.voyager.commons.model.geoname.fields.*;
import org.voyager.commons.model.geoname.query.GeoNearbyQuery;
import org.voyager.commons.model.geoname.query.GeoSearchQuery;
import org.voyager.commons.model.geoname.query.GeoTimezoneQuery;
import org.voyager.sdk.service.GeoService;
import org.voyager.sdk.service.TestServiceRegistry;
import org.voyager.sdk.service.utils.ServiceUtilsTestFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GeoServiceImplTest {
    static GeoService geoService;

    @BeforeEach
    void setup() {
        TestServiceRegistry.getInstance().registerTestImplementation(GeoService.class, GeoServiceImpl.class, ServiceUtilsTestFactory.getInstance());
        geoService = VoyagerServiceRegistry.getInstance().get(GeoService.class);
    }

    @Test
    void findNearbyPlaces() {
        assertThrows(NullPointerException.class,()->geoService.findNearbyPlaces(null));
        GeoNearbyQuery geoNearbyQuery = GeoNearbyQuery.builder().build();
        assertThrows(IllegalArgumentException.class,()->geoService.findNearbyPlaces(geoNearbyQuery));
        geoNearbyQuery.setLatitude(10.0);
        geoNearbyQuery.setLongitude(10.0);
        Either<ServiceError, List<GeoPlace>> either = geoService.findNearbyPlaces(geoNearbyQuery);
        assertTrue(either.isRight());

        geoNearbyQuery.setRadius(1);
        either = geoService.findNearbyPlaces(geoNearbyQuery);
        assertTrue(either.isRight());
    }

    @Test
    void getTimezone() {
        assertThrows(NullPointerException.class,()->geoService.getTimezone(null));
        GeoTimezoneQuery geoTimezoneQuery = GeoTimezoneQuery.builder().build();
        assertThrows(IllegalArgumentException.class,()->geoService.getTimezone(geoTimezoneQuery));
        geoTimezoneQuery.setLatitude(4.0);
        geoTimezoneQuery.setLongitude(4.0);
        Either<ServiceError, GeoTimezone> either = geoService.getTimezone(geoTimezoneQuery);
        assertTrue(either.isRight());

        geoTimezoneQuery.setRadius(10);
        geoTimezoneQuery.setLanguage("lang");
        geoTimezoneQuery.setDate("date");
        either = geoService.getTimezone(geoTimezoneQuery);
        assertTrue(either.isRight());
    }

    @Test
    void search() {
        assertThrows(NullPointerException.class,()->geoService.search(null));
        GeoSearchQuery geoSearchQuery = GeoSearchQuery.builder().query("query").build();
        Either<ServiceError, List<GeoPlace>> either = geoService.search(geoSearchQuery);
        assertTrue(either.isRight());

        geoSearchQuery = GeoSearchQuery.builder().query("qr").name("nm").nameEquals("ne").nameStartsWith("nsw")
                .countryList(List.of("to")).countryBias("cb").continentCode(Continent.OC).adminCode1("ac1")
                .adminCode2("ac2").adminCode3("ac3").adminCode4("ac4").adminCode5("ac5").featureClass(FeatureClass.P)
                .featureCode(FeatureCode.ADM2H).cities(CitySize.cities1000).style(XmlVerbosity.FULL)
                .isNameRequired(true).tag("tg").east(1f).west(1f).north(1f).south(1f).lang("lg").searchLang("sl")
                .orderBy(SearchOrder.population).inclBbox(true).build();

        either = geoService.search(geoSearchQuery);
        assertTrue(either.isRight());
    }

    @Test
    void getFull() {
        assertThrows(NullPointerException.class,()->geoService.getFull(null));
        Either<ServiceError, GeoFull> either = geoService.getFull(100L);
        assertTrue(either.isRight());
    }

    @Test
    void getCountries() {
        Either<ServiceError, List<GeoCountry>> either = geoService.getCountries();
        assertTrue(either.isRight());
    }
}