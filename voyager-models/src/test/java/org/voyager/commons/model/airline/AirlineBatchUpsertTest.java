package org.voyager.commons.model.airline;

import jakarta.validation.ValidationException;
import jakarta.validation.ValidatorFactory;
import org.apache.commons.lang3.Validate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.validate.ValidationUtils;

import javax.xml.validation.Validator;

import static org.junit.jupiter.api.Assertions.*;

class AirlineBatchUpsertTest {
    AirlineBatchUpsert airlineBatchUpsert;

    @Test
    void getAirline() {
        airlineBatchUpsert = AirlineBatchUpsert.builder().build();
        assertThrows(ValidationException.class,()-> ValidationUtils.validateAndThrow(airlineBatchUpsert));
    }

    @Test
    void getIataList() {
    }

    @Test
    void getIsActive() {
    }
}