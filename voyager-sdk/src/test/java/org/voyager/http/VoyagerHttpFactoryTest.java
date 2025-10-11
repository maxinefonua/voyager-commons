package org.voyager.http;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class VoyagerHttpFactoryTest {

    @Test
    void testAllMethods() throws URISyntaxException {
        VoyagerHttpFactory voyagerHttpFactory = new VoyagerHttpFactory();
        assertThrows(IllegalStateException.class, VoyagerHttpFactory::getClient);
        assertThrows(IllegalArgumentException.class,()->VoyagerHttpFactory.initialize(""));
        assertDoesNotThrow(()->VoyagerHttpFactory.initialize("test-token"));
        assertThrows(IllegalStateException.class,()->VoyagerHttpFactory.initialize("test-token"));
        assertNotNull(VoyagerHttpFactory.getClient());
        assertNotNull(VoyagerHttpFactory.request(new URI("http://test"),HttpMethod.GET));
        assertNotNull(VoyagerHttpFactory.request(new URI("http://test"),HttpMethod.POST,"test"));
        assertDoesNotThrow(VoyagerHttpFactory::reset);
        assertDoesNotThrow(()->VoyagerHttpFactory.initialize("test-token"));
        assertDoesNotThrow(VoyagerHttpFactory::reset);
    }
}