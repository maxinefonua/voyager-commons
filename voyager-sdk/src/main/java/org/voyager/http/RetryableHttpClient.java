package org.voyager.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.net.ConnectException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class RetryableHttpClient {
    private final HttpClient httpClient;
    private final int maxRetries;
    private final long initialDelayMillis;
    private final Semaphore requestLimiter;
    private final double backoffFactor;
    private final Logger LOGGER = LoggerFactory.getLogger(RetryableHttpClient.class);

    public RetryableHttpClient(int maxRetries, int maxConcurrentRequests, long initialDelayMillis, double backoffFactor) {
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .executor(Executors.newFixedThreadPool(3))
                .connectTimeout(Duration.ofSeconds(60))
                .build();
        this.maxRetries = maxRetries;
        this.initialDelayMillis = initialDelayMillis;
        this.backoffFactor = backoffFactor;
        this.requestLimiter = new Semaphore(maxConcurrentRequests);
    }

    public CompletableFuture<HttpResponse<String>> sendWithRetry(
            HttpRequest request,
            HttpResponse.BodyHandler<String> bodyHandler) {
        AtomicInteger attempt = new AtomicInteger(0);
        CompletableFuture<HttpResponse<String>> result = new CompletableFuture<>();
        attemptSend(request, bodyHandler, attempt, result);
        return result;
    }

    private void attemptSend(
            HttpRequest request,
            HttpResponse.BodyHandler<String> bodyHandler,
            AtomicInteger attempt,
            CompletableFuture<HttpResponse<String>> result) {
        if (attempt.get() >= maxRetries) {
            result.completeExceptionally(new MaxRetriesExceededException(maxRetries));
            return;
        }

        httpClient.sendAsync(request, bodyHandler)
                .whenComplete((response, throwable) -> {
                    requestLimiter.release();
                    if (throwable != null) {
                        handleFailure(request, bodyHandler, attempt, result, throwable);
                    } else if (response.statusCode() >= 500 || response.statusCode() == 429) {
                        handleRetryableResponse(request, bodyHandler, attempt, result, response);
                    } else {
                        result.complete(response);
                    }
                });
    }

    private void handleFailure(
            HttpRequest request,
            HttpResponse.BodyHandler<String> bodyHandler,
            AtomicInteger attempt,
            CompletableFuture<HttpResponse<String>> result,
            Throwable throwable) {

        if (isRetryable(throwable) && attempt.incrementAndGet() <= maxRetries) {
            long delay = calculateBackoff(attempt.get());
            LOGGER.info(String.format("attempting %d/%d retries after %dms",
                    attempt.get(),maxRetries,delay));
            scheduleRetry(request, bodyHandler, attempt, result, delay);
        } else {
            result.completeExceptionally(throwable);
        }
    }

    private void handleRetryableResponse(
            HttpRequest request,
            HttpResponse.BodyHandler<String> bodyHandler,
            AtomicInteger attempt,
            CompletableFuture<HttpResponse<String>> result,
            HttpResponse<String> response) {

        if (attempt.incrementAndGet() <= maxRetries) {
            long delay = response.statusCode() == 429
                    ? parseRetryAfter(response)
                    : calculateBackoff(attempt.get());
            LOGGER.info(String.format("attempting %d/%d retries after %dms",
                    attempt.get(),maxRetries,delay));
            scheduleRetry(request, bodyHandler, attempt, result, delay);
        } else {
            result.completeExceptionally(new ServerErrorException(response.statusCode()));
        }
    }

    private void scheduleRetry(
            HttpRequest request,
            HttpResponse.BodyHandler<String> bodyHandler,
            AtomicInteger attempt,
            CompletableFuture<HttpResponse<String>> result,
            long delayMillis) {
        if (!requestLimiter.tryAcquire()) {
        CompletableFuture.delayedExecutor(delayMillis, TimeUnit.MILLISECONDS)
                .execute(() -> attemptSend(request, bodyHandler, attempt, result));
        }
    }

    private boolean isRetryable(Throwable throwable) {
        return throwable instanceof ConnectException ||
                throwable instanceof SSLException ||
                throwable instanceof HttpTimeoutException ||
                throwable.getMessage().contains("connection timed out");
    }

    private long calculateBackoff(int attempt) {
        return (long) (initialDelayMillis * Math.pow(backoffFactor, attempt - 1));
    }

    private long parseRetryAfter(HttpResponse<String> response) {
        return response.headers()
                .firstValueAsLong("Retry-After")
                .orElse(initialDelayMillis) * 1000; // Convert seconds to milliseconds
    }

    public static class MaxRetriesExceededException extends RuntimeException {
        public MaxRetriesExceededException(int maxRetries) {
            super("Maximum retry attempts (" + maxRetries + ") exceeded");
        }
    }

    public static class ServerErrorException extends RuntimeException {
        private final int statusCode;

        public ServerErrorException(int statusCode) {
            super("Server returned status code: " + statusCode);
            this.statusCode = statusCode;
        }

        public int getStatusCode() {
            return statusCode;
        }
    }
}