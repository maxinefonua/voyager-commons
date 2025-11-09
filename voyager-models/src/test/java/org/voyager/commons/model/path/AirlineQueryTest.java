package org.voyager.commons.model.path;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.AirlineAirportQuery;
import org.voyager.commons.model.geoname.fields.SearchOperator;
import org.voyager.commons.validate.ValidationUtils;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AirlineQueryTest {

    @Test
    void builder() {
        assertThrows(ValidationException.class,()-> ValidationUtils.validateAndThrow(AirlineAirportQuery.builder().build()));
    }

    @Test
    void builderIATAList() {
        assertThrows(NullPointerException.class,()-> AirlineAirportQuery.builder().iatalist(null));
        List<String> iataList = new ArrayList<>();
        iataList.add(null);
        assertThrows(NullPointerException.class,()-> AirlineAirportQuery.builder().iatalist(iataList).build());
        assertThrows(ValidationException.class,()-> ValidationUtils.validateAndThrow(
                AirlineAirportQuery.builder().iatalist(List.of("")).build()));
        assertThrows(ValidationException.class,()-> ValidationUtils.validateAndThrow(
                AirlineAirportQuery.builder().iatalist(List.of("123")).build()));
        AirlineAirportQuery airlineAirportQuery = AirlineAirportQuery.builder().iatalist(List.of("abc")).build();
        assertEquals("ABC", airlineAirportQuery.getIatalist().get(0));
    }

    @Test
    void resolveRequestURL() {
        AirlineAirportQuery airlineAirportQuery = AirlineAirportQuery.builder().iatalist(List.of("abc","def")).build();
        String requestURL = airlineAirportQuery.getRequestURL();
        assertNotNull(requestURL);
        assertEquals("/airlines?iata=ABC,DEF&operator=OR", requestURL);
    }

    @Test
    void resolveRequestURLWithAND() {
        AirlineAirportQuery airlineAirportQuery = AirlineAirportQuery.builder().iatalist(List.of("abc","def"))
                .operator(SearchOperator.AND).build();
        String requestURL = airlineAirportQuery.getRequestURL();
        assertNotNull(requestURL);
        assertEquals("/airlines?iata=ABC,DEF&operator=AND", requestURL);
    }
}