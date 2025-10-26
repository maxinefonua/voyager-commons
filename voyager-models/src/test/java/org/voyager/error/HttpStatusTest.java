package org.voyager.error;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HttpStatusTest {
    private static HttpStatus HTTP_STATUS;
    private static String NOT_IMPLEMENTED_DESC = "Not Implemented";
    private static int NOT_IMPLEMENTED_STATUS_CODE = 501;

    @BeforeEach
    void setUp() {
        HTTP_STATUS = HttpStatus.NOT_IMPLEMENTED;
    }

    @Test
    @DisplayName("test constructor")
    void testConstructor() {
        assertNotNull(HTTP_STATUS);
    }

    @Test
    @DisplayName("test invalid http status code")
    public void testInvalidStatusCode() {
        assertThrows(IllegalArgumentException.class,() -> HttpStatus.getStatusFromCode(700));
    }

    @Test
    void getStatusFromCode() {
        assertEquals(HttpStatus.BAD_REQUEST,HttpStatus.getStatusFromCode(400));
    }

    @Test
    void getCode() {
        assertEquals(NOT_IMPLEMENTED_STATUS_CODE,HTTP_STATUS.getCode());
    }

    @Test
    void getDescription() {
        assertEquals(NOT_IMPLEMENTED_DESC,HTTP_STATUS.getDescription());
    }
}