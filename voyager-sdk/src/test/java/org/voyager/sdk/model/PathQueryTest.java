package org.voyager.sdk.model;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.sdk.model.RoutePathQuery;

import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PathQueryTest {
    @Test
    void getRequestURL() {
        assertThrows(NullPointerException.class,()-> RoutePathQuery.builder().build());

        RoutePathQuery routePathQuery = RoutePathQuery.builder().withOriginIATAList(List.of("hnd","nrt","kix"))
                .withDestinationIATAList(List.of("sjc","sfo","oak")).withLimit(10)
                .withExcludeIATAList(List.of("jfk","sea")).withExcludeRouteIdList(List.of(122,455))
                .withExcludeFlightNumberList(List.of("WN234","AA9871")).build();
        assertEquals("/route-path?origin=HND,NRT,KIX&destination=SJC,SFO,OAK&exclude=JFK,SEA&excludeRoute=122,455&excludeFlight=WN234,AA9871&limit=10",
                routePathQuery.getRequestURL());
    }

    @Test
    void builderOriginIATAListAndDestinationIATAList() {
        // set fields as null
        assertThrows(NullPointerException.class,()->
                RoutePathQuery.builder().withOriginIATAList(null).build());
        assertThrows(NullPointerException.class,()->
                RoutePathQuery.builder().withDestinationIATAList(null).build());

        // set only one field
        assertThrows(NullPointerException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of()).build());
        assertThrows(NullPointerException.class,()->
                RoutePathQuery.builder().withDestinationIATAList(List.of()).build());

        // set empty list
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of())
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of()).build());

        // set to list with invalid element
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("to"))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("sj")).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("143"))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("987")).build());

        // set to list with empty element
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of(""))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("")).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("   "))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("   ")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(listWithNullElement).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(listWithNullElement)
                        .withDestinationIATAList(List.of("abc")).build());

        // valid values
        RoutePathQuery routePathQuery = RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc")).build();
        assertEquals("AKL", routePathQuery.getOriginIATAList().get(0));
        assertEquals("SJC", routePathQuery.getDestinationIATAList().get(0));
        assertEquals("/route-path?origin=AKL&destination=SJC", routePathQuery.getRequestURL());
    }

    @Test
    void buildExcludeIATAList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeIATAList(null).build());

        // set to list with empty string
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","")).build());

        // set to list with blank string
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","   ")).build());

        // set to list with invalid string
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","123")).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","yo")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(listWithNullElement).build());

        // valid values
        RoutePathQuery routePathQuery = RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withExcludeIATAList(List.of("slc")).build();
        assertEquals("SLC", routePathQuery.getExcludeIATAList().get(0));
        assertEquals("/route-path?origin=AKL&destination=SJC&exclude=SLC", routePathQuery.getRequestURL());
    }

    @Test
    void buildExcludeRouteIdList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeRouteIdList(null).build());

        // set to list with null element
        List<Integer> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeRouteIdList(listWithNullElement).build());

        // valid values
        RoutePathQuery routePathQuery = RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withExcludeRouteIdList(List.of(123,456)).build();
        assertEquals(456, routePathQuery.getExcludeRouteIdList().get(1));
        assertEquals("/route-path?origin=AKL&destination=SJC&excludeRoute=123,456",
                routePathQuery.getRequestURL());
    }

    @Test
    void buildExcludeFlightNumberList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeFlightNumberList(null).build());

        // set to list with empty string
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("mn12","i2h3","")).build());

        // set to list with blank string
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("cd234","cd244","   ")).build());

        // set to list with invalid string
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("cd234","cd244","12  wq ")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(listWithNullElement).build());

        // valid values
        RoutePathQuery routePathQuery = RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withExcludeFlightNumberList(List.of("SDV123")).build();
        assertEquals("SDV123", routePathQuery.getExcludeFlightNumberList().get(0));
        assertEquals("/route-path?origin=AKL&destination=SJC&excludeFlight=SDV123", routePathQuery.getRequestURL());
    }

    @Test
    void buildLimit() {
        // set as null
        assertThrows(NullPointerException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(null).build());

        // set to invalid values
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(0).build());
        assertThrows(ValidationException.class,()->
                RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(27).build());

        // valid values
        RoutePathQuery routePathQuery = RoutePathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withLimit(21).build();
        assertEquals(21, routePathQuery.getLimit());
        assertEquals("/route-path?origin=AKL&destination=SJC&limit=21",
                routePathQuery.getRequestURL());
    }
}