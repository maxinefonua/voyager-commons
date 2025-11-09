package org.voyager.commons.model.flight;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.ValidationUtils;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FlightQueryTest {

    @Test
    void builderFlightNumber() {
        // fails on empty string
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                FlightNumberQuery.builder().flightNumber("").build()));
        // fails on blank string
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                FlightNumberQuery.builder().flightNumber("  ").build()));

        // valid field
        FlightNumberQuery flightQuery = FlightNumberQuery.builder().flightNumber("TS123").build();
        assertEquals("TS123",flightQuery.getFlightNumber());
        assertEquals("/flights?page=0&size=20&flightNumber=TS123",flightQuery.getRequestURL());
    }

    @Test
    void builderIsActive() {
        // valid field
        FlightQuery flightQuery = FlightQuery.builder().isActive(true).build();
        assertEquals(true,flightQuery.getIsActive());
        assertEquals("/flights?isActive=true&page=0&size=20",flightQuery.getRequestURL());
    }

    @Test
    void builderAirline() {
        // valid field
        FlightAirlineQuery flightQuery = FlightAirlineQuery.builder().airline(Airline.AIRCHINA).build();
        assertEquals(Airline.AIRCHINA,flightQuery.getAirline());
        assertEquals("/flights?page=0&size=20&airline=AIRCHINA",flightQuery.getRequestURL());
    }

    @Test
    void builderRouteIdList() {
        // fails on null element
        List<Integer> routeIdList = new ArrayList<>();
        routeIdList.add(null);
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                FlightQuery.builder().routeIdList(routeIdList).build()));

        // valid field
        routeIdList.remove(null);
        routeIdList.add(999);
        routeIdList.add(888);
        FlightQuery flightQuery = FlightQuery.builder().routeIdList(routeIdList).build();
        assertEquals(888,flightQuery.getRouteIdList().get(1));
        assertEquals("/flights?routeId=999,888&page=0&size=20",flightQuery.getRequestURL());
    }

    @Test
    void resolveRequestURL() {
        FlightAirlineQuery flightQuery = FlightAirlineQuery.builder().airline(Airline.SOUTHWEST)
                .isActive(false).routeIdList(List.of(123,456,789)).build();
        assertEquals("/flights?routeId=123,456,789&isActive=false&page=0&size=20&airline=SOUTHWEST",
                flightQuery.getRequestURL());
    }
}