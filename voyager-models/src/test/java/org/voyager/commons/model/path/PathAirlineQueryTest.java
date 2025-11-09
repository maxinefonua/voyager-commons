package org.voyager.commons.model.path;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.model.path.airline.PathAirlineQuery;
import org.voyager.commons.validate.ValidationUtils;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PathAirlineQueryTest {

    @Test
    void getRequestURL() {
        assertThrows(ValidationException.class,()->
                ValidationUtils.validateAndThrow(PathAirlineQuery.builder().build()));

        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().originSet(Set.of("HND","NRT","KIX"))
                .destinationSet(Set.of("SJC","SFO","OAK")).airline(Airline.JAPAN).size(10)
                .excludeSet(Set.of("JFK","SEA")).excludeRouteIdSet(Set.of(122,455))
                .excludeFlightNumberSet(Set.of("WN234","AA987")).build();
        assertEquals("/airline-path?origin=HND,KIX,NRT&destination=OAK,SFO,SJC&airline=JAPAN&excludeRoute=122,455&exclude=JFK,SEA&excludeFlight=AA987,WN234&page=0&size=10",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void builderOriginIATAListAndDestinationIATAList() {
        // set fields as null
        assertThrows(ValidationException.class,()-> ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(null).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().destinationSet(null).build()));

        // set only one field
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of()).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().destinationSet(Set.of()).build()));

        // set empty list
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of())
                        .destinationSet(Set.of("SJC")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of()).build()));

        // set to list with invalid element
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("to"))
                        .destinationSet(Set.of("SJC")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("ton"))
                        .destinationSet(Set.of("sj")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("143"))
                        .destinationSet(Set.of("SJC")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("ton"))
                        .destinationSet(Set.of("987")).build()));

        // set to list with empty element
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of(""))
                        .destinationSet(Set.of("SJC")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("ton"))
                        .destinationSet(Set.of("")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("   "))
                        .destinationSet(Set.of("SJC")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("ton"))
                        .destinationSet(Set.of("   ")).build()));

        // set to list with null element
        Set<String> setWithNullElement = new HashSet<>();
        setWithNullElement.add(null);
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("ton"))
                        .destinationSet(setWithNullElement).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(setWithNullElement)
                        .destinationSet(Set.of("abc")).build()));

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().originSet(Set.of("AKL"))
                .destinationSet(Set.of("SJC")).build();
        assertEquals("AKL", pathAirlineQuery.getOriginSet().stream().findFirst().get());
        assertEquals("SJC", pathAirlineQuery.getDestinationSet().stream().findFirst().get());
        assertEquals("/airline-path?origin=AKL&destination=SJC&page=0&size=5", pathAirlineQuery.getRequestURL());
    }

    @Test
    void getAirline() {
        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().originSet(Set.of("AKL"))
                .destinationSet(Set.of("SJC")).airline(Airline.JAPAN).build();
        assertEquals(Airline.JAPAN, pathAirlineQuery.getAirline());
        assertEquals("/airline-path?origin=AKL&destination=SJC&airline=JAPAN&page=0&size=5",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void buildExcludeIATAList() {
        // set to list with empty string
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeSet(Set.of("abc","dog","")).build()));

        // set to list with blank string
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeSet(Set.of("abc","dog","   ")).build()));

        // set to list with invalid string
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeSet(Set.of("ABC","DOG","123")).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeSet(Set.of("ABC","DOG","YO")).build()));

        // set with null element
        Set<String> setWithNullElement = new HashSet<>();
        setWithNullElement.add(null);
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeSet(setWithNullElement).build()));

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeSet(Set.of("SLC")).build();
        assertEquals("SLC", pathAirlineQuery.getExcludeSet().stream().findFirst().get());
        assertEquals("/airline-path?origin=AKL&destination=SJC&exclude=SLC&page=0&size=5",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void buildExcludeRouteIdList() {
        // set with null element
        Set<Integer> setWithNullElement = new HashSet<>();
        setWithNullElement.add(null);
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeRouteIdSet(setWithNullElement).build()));

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().originSet(Set.of("AKL"))
                .destinationSet(Set.of("SJC"))
                .excludeRouteIdSet(Set.of(123,456)).build();
        assertTrue(pathAirlineQuery.getExcludeRouteIdSet().stream().anyMatch(elem -> elem.equals(456)));
        assertEquals("/airline-path?origin=AKL&destination=SJC&excludeRoute=123,456&page=0&size=5",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void buildExcludeFlightNumberList() {
        // set to list with empty string
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeFlightNumberSet(Set.of("mn12","i2h3","")).build()));

        // set to list with blank string
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeFlightNumberSet(Set.of("cd234","cd244","   ")).build()));

        // set to list with invalid string
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeFlightNumberSet(Set.of("cd234","cd244","12  wq ")).build()));

        // set with null element
        Set<String> setWithNullElement = new HashSet<>();
        setWithNullElement.add(null);
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC"))
                        .excludeFlightNumberSet(setWithNullElement).build()));

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().originSet(Set.of("AKL"))
                .destinationSet(Set.of("SJC"))
                .excludeFlightNumberSet(Set.of("SDV123")).build();
        assertEquals("/airline-path?origin=AKL&destination=SJC&excludeFlight=SDV123&page=0&size=5",
                pathAirlineQuery.getRequestURL());
    }

    @Test
    void buildLimit() {
        // set to invalid values
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC")).size(0).build()));
        assertThrows(ValidationException.class,()->ValidationUtils.validateAndThrow(
                PathAirlineQuery.builder().originSet(Set.of("AKL"))
                        .destinationSet(Set.of("SJC")).size(20).build()));

        // valid values
        PathAirlineQuery pathAirlineQuery = PathAirlineQuery.builder().originSet(Set.of("AKL"))
                .destinationSet(Set.of("SJC"))
                .size(10).build();
        assertEquals(10, pathAirlineQuery.getSize());
        assertEquals("/airline-path?origin=AKL&destination=SJC&page=0&size=10",
                pathAirlineQuery.getRequestURL());
    }
}