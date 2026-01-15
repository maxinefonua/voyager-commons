package org.voyager.sdk.model;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.commons.validate.ValidationUtils;
import org.voyager.sdk.model.AirportQuery;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirportQueryTest {

    @Test
    void resolveRequestURL() {
        AirportQuery airportQuery = AirportQuery.builder()
                .airportTypeList(List.of(AirportType.CIVIL,AirportType.MILITARY)).build();
        assertEquals("/airports?page=0&size=100&type=CIVIL,MILITARY",airportQuery.getRequestURL());

        airportQuery = AirportQuery.builder()
                .countryCode("us").build();
        assertEquals("/airports?page=0&size=100&countryCode=US",airportQuery.getRequestURL());

        airportQuery = AirportQuery.builder()
                .airlineList(List.of(Airline.AIRNZ)).build();
        assertEquals("/airports?page=0&size=100&airline=AIRNZ",airportQuery.getRequestURL());

        airportQuery = AirportQuery.builder().airlineList(List.of(Airline.JAPAN))
                .airportTypeList(List.of(AirportType.CIVIL,AirportType.MILITARY)).countryCode("us").build();
        assertEquals("/airports?page=0&size=100&countryCode=US&airline=JAPAN&type=CIVIL,MILITARY",
                airportQuery.getRequestURL());
    }

    @Test
    void builder() {
        // defaults set page and size
        assertDoesNotThrow(()-> ValidationUtils.validateAndThrow(AirportQuery.builder().build()));
        // all valid
        AirportQuery airportQuery = AirportQuery.builder().airlineList(List.of(Airline.JAPAN))
                .airportTypeList(List.of(AirportType.CIVIL,AirportType.MILITARY)).countryCode("us").build();
        assertEquals(List.of(AirportType.CIVIL,AirportType.MILITARY),airportQuery.getAirportTypeList());
        assertEquals(Airline.JAPAN,airportQuery.getAirlineList().get(0));
    }

    @Test
    void builderCountryCode() {
        // invalid country code values
        assertThrows(ValidationException.class,()->
                ValidationUtils.validateAndThrow(AirportQuery.builder().countryCode("").build()));
        assertThrows(ValidationException.class,()->
                ValidationUtils.validateAndThrow(AirportQuery.builder().countryCode("abs").build()));
        assertThrows(ValidationException.class,()->
                ValidationUtils.validateAndThrow(AirportQuery.builder().countryCode("34").build()));
    }

    @Test
    void builderTypeList() {
        // nonnull type list
        List<AirportType> airportTypeList = new ArrayList<>();
        airportTypeList.add(null);
        assertThrows(org.voyager.commons.error.ValidationException.class,()-> ValidationUtils.validateAndThrow(
                AirportQuery.builder().airportTypeList(airportTypeList).build()));
        // valid type list
        airportTypeList.remove(null);
        airportTypeList.add(AirportType.UNVERIFIED);
        AirportQuery airportQuery = AirportQuery.builder().airportTypeList(airportTypeList).build();
        assertEquals(1,airportQuery.getAirportTypeList().size());
        assertEquals(AirportType.UNVERIFIED,airportQuery.getAirportTypeList().get(0));
    }

    @Test
    void builderAirline() {
        // nonnull builder methods
        List<Airline> airlineList = new ArrayList<>();
        airlineList.add(null);
        assertThrows(ValidationException.class,()->
                ValidationUtils.validateAndThrow(AirportQuery.builder().airlineList(airlineList).build()));
        // valid airline
        AirportQuery airportQuery = AirportQuery.builder().airlineList(List.of(Airline.DELTA)).build();
        assertEquals(Airline.DELTA,airportQuery.getAirlineList().get(0));
    }
}