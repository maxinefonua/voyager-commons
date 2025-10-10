package org.voyager.model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PathAirlineQueryTest {

    @Test
    void getRequestURL() {
        assertThrows(NullPointerException.class,()->PathAirlineQuery.builder().build());

        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().withOriginIATAList(List.of("hnd","nrt","kix"))
                .withDestinationIATAList(List.of("sjc","sfo","oak")).withAirline(Airline.JAPAN).withLimit(10)
                .withExcludeIATAList(List.of("jfk","sea")).withExcludeRouteIdList(List.of(122,455))
                .withExcludeFlightNumberList(List.of("wn234","aa987")).build();
        assertEquals("/path-airline?origin=HND,NRT,KIX&destination=SJC,SFO,OAK&airline=JAPAN&excludeRoute=122,455&exclude=JFK,SEA&excludeFlight=WN234,AA987&limit=10",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void builderOriginIATAListAndDestinationIATAList() {
        // set fields as null
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(null).build());
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withDestinationIATAList(null).build());

        // set only one field
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of()).build());
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withDestinationIATAList(List.of()).build());

        // set empty list
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of())
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of()).build());

        // set to list with invalid element
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("to"))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("sj")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("143"))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("987")).build());

        // set to list with empty element
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of(""))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("   "))
                        .withDestinationIATAList(List.of("sjc")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(List.of("   ")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("ton"))
                        .withDestinationIATAList(listWithNullElement).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(listWithNullElement)
                        .withDestinationIATAList(List.of("abc")).build());

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc")).build();
        assertEquals("AKL",pathAirlineQuery.getOriginIATAList().get(0));
        assertEquals("SJC",pathAirlineQuery.getDestinationIATAList().get(0));
        assertEquals("/path-airline?origin=AKL&destination=SJC",pathAirlineQuery.getRequestURL());
    }

    @Test
    void getAirline() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withAirline(null).build());

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc")).withAirline(Airline.JAPAN).build();
        assertEquals(Airline.JAPAN,pathAirlineQuery.getAirline());
        assertEquals("/path-airline?origin=AKL&destination=SJC&airline=JAPAN",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void buildExcludeIATAList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeIATAList(null).build());

        // set empty list
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeIATAList(List.of()).build());

        // set to list with empty string
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","")).build());

        // set to list with blank string
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","   ")).build());

        // set to list with invalid string
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","123")).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("abc","dog","yo")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(listWithNullElement).build());

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeIATAList(List.of("slc")).build();
        assertEquals("SLC",pathAirlineQuery.getExcludeIATAList().get(0));
        assertEquals("/path-airline?origin=AKL&destination=SJC&exclude=SLC",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void buildExcludeRouteIdList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeRouteIdList(null).build());

        // set empty list
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeRouteIdList(List.of()).build());

        // set to list with null element
        List<Integer> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeRouteIdList(listWithNullElement).build());

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withExcludeRouteIdList(List.of(123,456)).build();
        assertEquals(456,pathAirlineQuery.getExcludeRouteIdList().get(1));
        assertEquals("/path-airline?origin=AKL&destination=SJC&excludeRoute=123,456",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void buildExcludeFlightNumberList() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeFlightNumberList(null).build());

        // set empty list
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withExcludeFlightNumberList(List.of()).build());

        // set to list with empty string
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("mn12","i2h3","")).build());

        // set to list with blank string
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("cd234","cd244","   ")).build());

        // set to list with invalid string
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(List.of("cd234","cd244","12  wq ")).build());

        // set to list with null element
        List<String> listWithNullElement = new ArrayList<>();
        listWithNullElement.add(null);
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc"))
                        .withExcludeFlightNumberList(listWithNullElement).build());

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withExcludeFlightNumberList(List.of("sdv123")).build();
        assertEquals("SDV123",pathAirlineQuery.getExcludeFlightNumberList().get(0));
        assertEquals("/path-airline?origin=AKL&destination=SJC&excludeFlight=SDV123",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void buildLimit() {
        // set as null
        assertThrows(NullPointerException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(null).build());

        // set to invalid values
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(0).build());
        assertThrows(IllegalArgumentException.class,()->
                PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                        .withDestinationIATAList(List.of("sjc")).withLimit(20).build());

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().withOriginIATAList(List.of("akl"))
                .withDestinationIATAList(List.of("sjc"))
                .withLimit(10).build();
        assertEquals(10,pathAirlineQuery.getLimit());
        assertEquals("/path-airline?origin=AKL&destination=SJC&limit=10",
                pathAirlineQuery.getRequestURL());
    }
}