package org.voyager.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AirlineQueryTest {

    @Test
    void builder() {
        assertThrows(NullPointerException.class,()->AirlineQuery.builder().build());
    }

    @Test
    void builderIATAList() {
        assertThrows(NullPointerException.class,()->AirlineQuery.builder().withIATAList(null));
        List<String> iataList = new ArrayList<>();
        assertThrows(IllegalArgumentException.class,()->AirlineQuery.builder().withIATAList(iataList).build());
        iataList.add(null);
        assertThrows(IllegalArgumentException.class,()->AirlineQuery.builder().withIATAList(iataList).build());
        assertThrows(IllegalArgumentException.class,()->AirlineQuery.builder().withIATAList(List.of("")).build());
        assertThrows(IllegalArgumentException.class,()->AirlineQuery.builder().withIATAList(List.of("123")).build());
        AirlineQuery airlineQuery = AirlineQuery.builder().withIATAList(List.of("abc")).build();
        assertEquals("ABC", airlineQuery.getIATAList().get(0));
    }

    @Test
    void resolveRequestURL() {
        AirlineQuery airlineQuery = AirlineQuery.builder().withIATAList(List.of("abc","def")).build();
        String requestURL = airlineQuery.getRequestURL();
        assertNotNull(requestURL);
        assertEquals("/airport-airlines?iata=ABC,DEF", requestURL);
    }
}