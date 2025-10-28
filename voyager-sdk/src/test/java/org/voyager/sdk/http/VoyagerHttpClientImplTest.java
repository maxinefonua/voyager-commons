package org.voyager.sdk.http;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.commons.error.ServiceError;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VoyagerHttpClientImplTest {
    VoyagerHttpClient voyagerHttpClient;

    @BeforeEach
    void setUp() {
        voyagerHttpClient = new VoyagerHttpClientImpl(new MockHttpClient());
    }

    @Test
    void send() {
        Either<ServiceError, HttpResponse<String>> either = voyagerHttpClient.send(new MockHttpRequest());
        assertNotNull(either);
        voyagerHttpClient = new VoyagerHttpClientImpl(new MockHttpClient(true));
        either = voyagerHttpClient.send(new MockHttpRequest());
        assertNotNull(either);
        assertTrue(either.isLeft());
    }
}