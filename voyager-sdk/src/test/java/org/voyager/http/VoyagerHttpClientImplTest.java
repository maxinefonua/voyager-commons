package org.voyager.http;

import io.vavr.control.Either;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.voyager.error.ServiceError;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

class VoyagerHttpClientImplTest {
    VoyagerHttpClient voyagerHttpClient;

    @BeforeEach
    void setUp() {
        voyagerHttpClient = new VoyagerHttpClientImpl(new MockHttpClient());
    }

    @Test
    void sendAsync() {
        CompletableFuture<HttpResponse<String>> completableFuture = voyagerHttpClient.sendAsync(new MockHttpRequest());
        assertNotNull(completableFuture);
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