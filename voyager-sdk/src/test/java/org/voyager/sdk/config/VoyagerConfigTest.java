package org.voyager.sdk.config;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.voyager.sdk.config.Protocol;
import org.voyager.sdk.config.VoyagerConfig;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class VoyagerConfigTest {
    private static VoyagerConfig validVoyagerConfig;
    private static final String HOST = "host";
    private static final int PORT = 8080;
    private static final String AUTH_TOKEN = "auth-token";
    private static final String BASE_URL = "http://host:8080";

    @BeforeAll
    public static void init() {
        validVoyagerConfig = new VoyagerConfig(Protocol.HTTP,HOST,PORT,AUTH_TOKEN);
    }

    @Test
    @DisplayName("valid constructor")
    public void testConstructor() {
        assertNotNull(validVoyagerConfig);
        assertEquals(BASE_URL,validVoyagerConfig.getBaseURL());
        assertEquals(AUTH_TOKEN,validVoyagerConfig.getAuthorizationToken());

        VoyagerConfig voyagerConfig = new VoyagerConfig(Protocol.HTTP,"testhost","test-token");
        assertEquals("http://testhost",voyagerConfig.getBaseURL());
        assertEquals("test-token",voyagerConfig.getAuthorizationToken());
    }

    @Test
    @DisplayName("null args")
    public void testConstructorNullExceptions() {
        Exception exception = assertThrows(NullPointerException.class,() -> new VoyagerConfig(null,HOST,PORT,AUTH_TOKEN));
        String expected = "protocol is marked non-null but is null";
        assertEquals(expected,exception.getMessage());

        exception = assertThrows(NullPointerException.class,() -> new VoyagerConfig(null,null,PORT,null));
        expected = "protocol is marked non-null but is null";
        assertEquals(expected,exception.getMessage());

        exception = assertThrows(NullPointerException.class,() -> new VoyagerConfig(Protocol.HTTP,null,PORT,AUTH_TOKEN));
        expected = "host is marked non-null but is null";
        assertEquals(expected,exception.getMessage());

        exception = assertThrows(NullPointerException.class,() -> new VoyagerConfig(Protocol.HTTP,HOST,PORT,null));
        expected = "authorizationToken is marked non-null but is null";
        assertEquals(expected,exception.getMessage());

        assertThrows(NullPointerException.class,()->new VoyagerConfig(null,"testhost","test-token"));
        assertThrows(NullPointerException.class,()->new VoyagerConfig(null,"testhost","test-token",true));
        assertThrows(NullPointerException.class,()->new VoyagerConfig(Protocol.HTTP,null,"test-token"));
        assertThrows(NullPointerException.class,()->new VoyagerConfig(Protocol.HTTP,null,"test-token",true));
        assertThrows(NullPointerException.class,()->new VoyagerConfig(Protocol.HTTP,"testhost",null));
        assertThrows(NullPointerException.class,()->new VoyagerConfig(Protocol.HTTP,"testhost",null,true));
    }

    @Test
    @DisplayName("invalid args")
    public void testInvalidProtocol() {
        String invalidHost = "invalid/";
        Exception exception = assertThrows(Exception.class, () -> new VoyagerConfig(Protocol.HTTP,invalidHost,PORT,AUTH_TOKEN));
        String expected = "Illegal character found in host: '/'";
        assertEquals(expected, exception.getMessage());
    }
}