package org.voyager.commons.model.route;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.ValidationUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RouteQueryTest {

    @Test
    void builderOrigin() {
        assertThrows(ValidationException.class,()-> ValidationUtils.validateAndThrow(
                RouteQuery.builder().build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().origin("").build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().origin("  ").build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().origin("as2").build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().origin("as").build()));

        RouteQuery routeQuery = RouteQuery.builder().origin("SJC").build();
        assertEquals("SJC",routeQuery.getOrigin());
        assertEquals("/routes?origin=SJC",routeQuery.getRequestURL());
    }

    @Test
    void builderDestination() {
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().destination("").build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().destination("  ").build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().destination("as2").build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().destination("as").build()));

        RouteQuery routeQuery = RouteQuery.builder().destination("SJC").build();
        assertEquals("SJC",routeQuery.getDestination());
        assertEquals("/routes?destination=SJC",routeQuery.getRequestURL());
    }

    @Test
    void getRequestURL() {
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().build()));

        RouteQuery routeQuery = RouteQuery.builder().origin("SLC").destination("HEL")
                .airline(Airline.JAPAN).build();
        assertEquals("/routes?origin=SLC&destination=HEL&airline=JAPAN",routeQuery.getRequestURL());
    }

    @Test
    void builderAirline() {
        RouteQuery routeQuery = RouteQuery.builder().airline(Airline.JAPAN).build();
        assertEquals(Airline.JAPAN,routeQuery.getAirline());
        assertEquals("/routes?airline=JAPAN",routeQuery.getRequestURL());
    }
}