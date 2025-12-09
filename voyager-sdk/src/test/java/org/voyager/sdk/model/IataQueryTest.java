package org.voyager.sdk.model;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.voyager.commons.model.airline.Airline;
import org.voyager.commons.validate.ValidationUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class IataQueryTest {

    @Test
    void builder() {
        assertDoesNotThrow(()->ValidationUtils.validateAndThrow(
                IataQuery.builder().airlineList(List.of(Airline.DELTA))));
    }

    @Test
    void getAirlineList() {
        List<Airline> airlineList = new ArrayList<>();
        airlineList.add(null);
        assertThrows(ValidationException.class,()->
                ValidationUtils.validateAndThrow(IataQuery.builder().airlineList(airlineList).build()));
    }
}