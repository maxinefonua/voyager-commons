package org.voyager.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathQueryTest {
    @Test
    void getRequestURL() {
        assertThrows(NullPointerException.class,()->PathQuery.builder().build());

        PathQuery pathQuery = PathQuery.builder().withOriginIATAList(List.of("hnd","nrt","kix"))
                .withDestinationIATAList(List.of("sjc","sfo","oak")).withLimit(10)
                .withExcludeIATAList(List.of("jfk","sea")).withExcludeRouteIdList(List.of(122,455))
                .withExcludeFlightNumberList(List.of("wn234","aa987")).build();
        assertEquals("/path?origin=HND,NRT,KIX&destination=SJC,SFO,OAK&exclude=JFK,SEA&excludeRoute=122,455&excludeFlight=WN234,AA987&limit=10",
                pathQuery.getRequestURL());
    }

    @Test
    void builderOriginIATAListAndDestinationIATAList() {
        // set fields as null
        assertThrows(NullPointerException.class,()->
                PathQuery.builder().withOriginIATAList(null).build());
        assertThrows(NullPointerException.class,()->
                PathQuery.builder().withDestinationIATAList(null).build());

        // set only one field
        assertThrows(NullPointerException.class,()->
                PathQuery.builder().withOriginIATAList(List.of()).build());
        assertThrows(NullPointerException.class,()->
                PathQuery.builder().withDestinationIATAList(List.of()).build());

        // set empty list
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of())
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of()).build());

        // set to list with invalid element
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("to"))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("sj")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("143"))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("987")).build());

        // set to list with empty element
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of(""))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("   "))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("   ")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(listWithNullElement).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(listWithNullElement)
                        .withDestinationIATAList(List.of("abc")).build());

        // valid values
        PathQuery pathQuery = PathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc")).build();
        assertEquals("AKL",pathQuery.getOriginIATAList().get(0));
        assertEquals("SJC",pathQuery.getDestinationIATAList().get(0));
        assertEquals("/path?origin=AKL&destination=SJC",pathQuery.getRequestURL());
    }

    @Test
    void buildExcludeIATAList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeIATAList(null).build());

        // set empty list
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeIATAList(List.of()).build());

        // set to list with empty string
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","")).build());

        // set to list with blank string
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","   ")).build());

        // set to list with invalid string
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","123")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","yo")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(listWithNullElement).build());

        // valid values
        PathQuery pathQuery = PathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withExcludeIATAList(List.of("slc")).build();
        assertEquals("SLC",pathQuery.getExcludeIATAList().get(0));
        assertEquals("/path?origin=AKL&destination=SJC&exclude=SLC", pathQuery.getRequestURL());
    }

    @Test
    void buildExcludeRouteIdList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeRouteIdList(null).build());

        // set empty list
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeRouteIdList(List.of()).build());

        // set to list with null element
        List<Integer> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeRouteIdList(listWithNullElement).build());

        // valid values
        PathQuery pathQuery = PathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withExcludeRouteIdList(List.of(123,456)).build();
        assertEquals(456,pathQuery.getExcludeRouteIdList().get(1));
        assertEquals("/path?origin=AKL&destination=SJC&excludeRoute=123,456",
                pathQuery.getRequestURL());
    }

    @Test
    void buildExcludeFlightNumberList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeFlightNumberList(null).build());

        // set empty list
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeFlightNumberList(List.of()).build());

        // set to list with empty string
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("mn12","i2h3","")).build());

        // set to list with blank string
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("cd234","cd244","   ")).build());

        // set to list with invalid string
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("cd234","cd244","12  wq ")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(listWithNullElement).build());

        // valid values
        PathQuery pathQuery = PathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withExcludeFlightNumberList(List.of("sdv123")).build();
        assertEquals("SDV123",pathQuery.getExcludeFlightNumberList().get(0));
        assertEquals("/path?origin=AKL&destination=SJC&excludeFlight=SDV123", pathQuery.getRequestURL());
    }

    @Test
    void buildLimit() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(null).build());

        // set to invalid values
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(0).build());
        assertThrows(IllegalArgumentException.class,()->
                PathQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(27).build());

        // valid values
        PathQuery pathQuery = PathQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withLimit(21).build();
        assertEquals(21,pathQuery.getLimit());
        assertEquals("/path?origin=AKL&destination=SJC&limit=21",
                pathQuery.getRequestURL());
    }
}