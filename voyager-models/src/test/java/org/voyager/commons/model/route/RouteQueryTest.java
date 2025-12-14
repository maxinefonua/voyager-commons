package org.voyager.commons.model.route;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.ValidationUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RouteQueryTest {

    @Test
    void builderOrigin() {
        assertThrows(ValidationException.class,()-> ValidationUtils.validateAndThrow(
                RouteQuery.builder().build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().originList(List.of("")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().originList(List.of("  ")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().originList(List.of("as2")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().originList(List.of("as")).build()));

        RouteQuery routeQuery = RouteQuery.builder().originList(List.of("SJC")).build();
        assertEquals("SJC",routeQuery.getOriginList().get(0));
        assertEquals("/routes?origin=SJC",routeQuery.getRequestURL());
    }

    @Test
    void builderDestination() {
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().destinationList(List.of("")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().destinationList(List.of("  ")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().destinationList(List.of("as2")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().destinationList(List.of("as")).build()));

        RouteQuery routeQuery = RouteQuery.builder().destinationList(List.of("SJC")).build();
        assertEquals("SJC",routeQuery.getDestinationList().get(0));
        assertEquals("/routes?destination=SJC",routeQuery.getRequestURL());
    }

    @Test
    void getRequestURL() {
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                RouteQuery.builder().build()));

        RouteQuery routeQuery = RouteQuery.builder().originList(List.of("SLC"))
                .destinationList(List.of("HEL")).build();
        assertEquals("/routes?origin=SLC&destination=HEL",routeQuery.getRequestURL());
    }
}