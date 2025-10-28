package org.voyager.sdk.model;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.airport.AirportType;
import org.voyager.sdk.model.NearbyAirportQuery;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NearbyAirportQueryTest {

    @Test
    void resolveRequestURL() {
        NearbyAirportQuery nearbyAirportQuery = NearbyAirportQuery.builder().withLatitude(10.0)
                .withLongitude(-10.0).withAirportTypeList(List.of(AirportType.CIVIL,AirportType.UNVERIFIED))
                .withAirlineList(List.of(Airline.DELTA,Airline.JAPAN)).withLimit(25).build();
        assertEquals("/nearby-airports?latitude=10.0&longitude=-10.0&limit=25&airline=DELTA,JAPAN&type=CIVIL,UNVERIFIED",
                nearbyAirportQuery.getRequestURL());
    }

    @Test
    void builderLatitudeAndLongitude() {
        // set fields as null
        assertThrows(NullPointerException.class,()->NearbyAirportQuery.builder().withLatitude(null).build());
        assertThrows(NullPointerException.class,()->NearbyAirportQuery.builder().withLongitude(null).build());
        // set one of two required fields
        assertThrows(NullPointerException.class,()->NearbyAirportQuery.builder().withLatitude(10.0).build());
        assertThrows(NullPointerException.class,()->NearbyAirportQuery.builder().withLongitude(10.0).build());

        // set over maximum
        assertThrows(ValidationException.class,()->
                NearbyAirportQuery.builder().withLatitude(900.0).withLongitude(10.0).build());
        assertThrows(ValidationException.class,()->
                NearbyAirportQuery.builder().withLatitude(10.0).withLongitude(800.0).build());

        // set under minimum
        assertThrows(ValidationException.class,()->
                NearbyAirportQuery.builder().withLatitude(-300.0).withLongitude(-10.0).build());
        assertThrows(ValidationException.class,()->
                NearbyAirportQuery.builder().withLatitude(-10.0).withLongitude(-400.0).build());

        // valid
        NearbyAirportQuery nearbyAirportQuery = NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).build();
        assertEquals(10.0,nearbyAirportQuery.getLatitude());
        assertEquals(-10.0,nearbyAirportQuery.getLongitude());
        assertEquals("/nearby-airports?latitude=10.0&longitude=-10.0",
                nearbyAirportQuery.getRequestURL());
    }

    @Test
    void builderLimit() {
        // set as null
        assertThrows(NullPointerException.class,()->NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withLimit(null).build());
        // set less than min
        assertThrows(ValidationException.class,()-> NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withLimit(0).build());

        // valid value
        NearbyAirportQuery nearbyAirportQuery = NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withLimit(20).build();
        assertEquals(20,nearbyAirportQuery.getLimit());
        assertEquals("/nearby-airports?latitude=10.0&longitude=-10.0&limit=20",
                nearbyAirportQuery.getRequestURL());
    }

    @Test
    void builderAirlineList() {
        // set as null
        assertThrows(NullPointerException.class,()->NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withAirlineList(null).build());
        // set as list with null
        List<Airline> airlineList = new ArrayList<>();
        airlineList.add(null);
        assertThrows(ValidationException.class,()-> NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withAirlineList(airlineList).build());

        // valid value
        airlineList.remove(null);
        airlineList.add(Airline.AIRNZ);
        airlineList.add(Airline.JAPAN);
        NearbyAirportQuery nearbyAirportQuery = NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withAirlineList(airlineList).build();
        assertEquals(Airline.AIRNZ,nearbyAirportQuery.getAirlineList().get(0));
        assertEquals("/nearby-airports?latitude=10.0&longitude=-10.0&airline=AIRNZ,JAPAN",
                nearbyAirportQuery.getRequestURL());
    }

    @Test
    void builderAirportTypeList() {
        // set as null
        assertThrows(NullPointerException.class,()->NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withAirportTypeList(null).build());
        // set as list with null
        List<AirportType> typeList = new ArrayList<>();
        typeList.add(null);
        assertThrows(ValidationException.class,()-> NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withAirportTypeList(typeList).build());

        // valid value
        typeList.remove(null);
        typeList.add(AirportType.CIVIL);
        typeList.add(AirportType.MILITARY);
        NearbyAirportQuery nearbyAirportQuery = NearbyAirportQuery.builder()
                .withLatitude(10.0).withLongitude(-10.0).withAirportTypeList(typeList).build();
        assertEquals(AirportType.CIVIL,nearbyAirportQuery.getAirportTypeList().get(0));
        assertEquals("/nearby-airports?latitude=10.0&longitude=-10.0&type=CIVIL,MILITARY",
                nearbyAirportQuery.getRequestURL());
    }
}