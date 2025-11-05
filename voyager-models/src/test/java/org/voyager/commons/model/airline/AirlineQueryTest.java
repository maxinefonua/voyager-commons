package org.voyager.commons.model.airline;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
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
        assertThrows(ValidationException.class,()-> ValidationUtils.validateAndThrow(AirlineQuery.builder().build()));
    }

    @Test
    void builderIATAList() {
        assertThrows(NullPointerException.class,()->AirlineQuery.builder().withIATAList(null));
        List<String> iataList = new ArrayList<>();
        iataList.add(null);
        assertThrows(ValidationException.class,()->AirlineQuery.builder().withIATAList(iataList).build());
        assertThrows(ValidationException.class,()->AirlineQuery.builder().withIATAList(List.of("")).build());
        assertThrows(ValidationException.class,()->AirlineQuery.builder().withIATAList(List.of("123")).build());
        AirlineQuery airlineQuery = AirlineQuery.builder().withIATAList(List.of("abc")).build();
        assertEquals("ABC", airlineQuery.getIATAList().get(0));
    }

    @Test
    void resolveRequestURL() {
        AirlineQuery airlineQuery = AirlineQuery.builder().withIATAList(List.of("abc","def")).build();
        String requestURL = airlineQuery.getRequestURL();
        assertNotNull(requestURL);
        assertEquals("/airlines?iata=ABC,DEF&operator=OR", requestURL);
    }

    @Test
    void resolveRequestURLWithAND() {
        AirlineQuery airlineQuery = AirlineQuery.builder().withIATAList(List.of("abc","def"))
                .withOperator(SearchOperator.AND).build();
        String requestURL = airlineQuery.getRequestURL();
        assertNotNull(requestURL);
        assertEquals("/airlines?iata=ABC,DEF&operator=AND", requestURL);
    }
}