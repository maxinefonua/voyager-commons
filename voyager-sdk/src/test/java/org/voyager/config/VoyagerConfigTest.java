package org.voyager.config;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VoyagerConfigTest {
    private static VoyagerConfig validVoyagerConfig;
    private static final String HOST = "host";
    private static final int PORT = 8080;
    private static final int MAX_THREADS = 100;
    private static final String AUTH_TOKEN = "auth-token";
    private static final String BASE_URL = "http://host:8080";
    private static final String AIRPORTS_PATH = "http://host:8080/airports";

    @BeforeAll
    public static void init() {
        validVoyagerConfig = new VoyagerConfig(Protocol.HTTP,HOST,PORT,MAX_THREADS,AUTH_TOKEN);
    }

    @Test
    @DisplayName("valid constructor")
    public void testConstructor() {
        assertNotNull(validVoyagerConfig);
        assertEquals(BASE_URL,validVoyagerConfig.getBaseURL());
        assertEquals(MAX_THREADS,validVoyagerConfig.getMaxThreads());
        assertEquals(AUTH_TOKEN,validVoyagerConfig.getAuthorizationToken());
    }

    @Test
    @DisplayName("null args")
    public void testConstructorNullExceptions() {
        Exception exception = assertThrows(NullPointerException.class,() -> new VoyagerConfig(null,HOST,PORT,MAX_THREADS,AUTH_TOKEN));
        String expected = "protocol is marked non-null but is null";
        assertEquals(expected,exception.getMessage());

        exception = assertThrows(NullPointerException.class,() -> new VoyagerConfig(null,null,PORT,MAX_THREADS,null));
        expected = "protocol is marked non-null but is null";
        assertEquals(expected,exception.getMessage());

        exception = assertThrows(NullPointerException.class,() -> new VoyagerConfig(Protocol.HTTP,null,PORT,MAX_THREADS,AUTH_TOKEN));
        expected = "host is marked non-null but is null";
        assertEquals(expected,exception.getMessage());

        exception = assertThrows(NullPointerException.class,() -> new VoyagerConfig(Protocol.HTTP,HOST,PORT,MAX_THREADS,null));
        expected = "authorizationToken is marked non-null but is null";
        assertEquals(expected,exception.getMessage());
    }

    @Test
    @DisplayName("invalid args")
    public void testInvalidProtocol() {
        String invalidHost = "invalid/";
        Exception exception = assertThrows(Exception.class, () -> new VoyagerConfig(Protocol.HTTP,invalidHost,PORT,MAX_THREADS,AUTH_TOKEN));
        String expected = "Illegal character found in host: '/'";
        assertEquals(expected, exception.getMessage());
    }
}