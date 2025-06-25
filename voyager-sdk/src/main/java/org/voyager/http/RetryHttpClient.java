package org.voyager.http;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

public class RetryHttpClient {
    private final HttpClient httpClient;
    private final AsyncHttpClient asyncHttpClient;
    private final int maxRetries;
    private final long retryDelayMillis;
    private final double backoffFactor;
    private final Predicate<HttpResponse<String>> shouldRetry;
    private final Logger LOGGER = LoggerFactory.getLogger(RetryHttpClient.class);
    private final ExecutorService executorService;

    public RetryHttpClient(ExecutorService executorService) {
        this(3, 10,2.0, defaultRetryCondition(),executorService);
    }

    public RetryHttpClient(int maxRetries, long retryDelaySeconds, double backoffFactor,
                           Predicate<HttpResponse<String>> shouldRetry, ExecutorService executorService) {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
        this.asyncHttpClient = Dsl.asyncHttpClient(
                Dsl.config()
                        .setMaxRequestRetry(maxRetries)
                        .setPooledConnectionIdleTimeout(60000)
                .setHandshakeTimeout(30000) // Specific SSL handshake timeout
                .setReadTimeout(30000));
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelaySeconds;
        this.backoffFactor = backoffFactor;
        this.shouldRetry = shouldRetry;
        this.executorService = executorService;
    }

    private class RetryHandler {
        private final AtomicInteger attempts = new AtomicInteger(0);
        private final Request request;
        private final int maxRetries;
        private final CompletableFuture<Response> resultFuture;

        RetryHandler(Request request, int maxRetries, CompletableFuture<Response> resultFuture) {
            this.request = request;
            this.maxRetries = maxRetries;
            this.resultFuture = resultFuture;
        }

        void attempt() {
            asyncHttpClient.executeRequest(request)
                    .toCompletableFuture()
                    .whenCompleteAsync((response, ex) -> {
                        if (ex != null) {
                            handleFailure(ex);
                        } else if (response.getStatusCode() == 429) {
                            handleRateLimit(response);
                        } else {
                            resultFuture.complete(response);
                        }
                    });
        }

        private void handleRateLimit(Response response) {
            long delayMs = parseRetryAfter(response);
            long jitter = ThreadLocalRandom.current().nextLong(delayMs / 2);
            delayMs += jitter;
            LOGGER.info(String.format("Rate limited (attempt %d/%d). Retrying after %dms",
                    attempts.incrementAndGet(), maxRetries, delayMs));
            CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS, executorService)
                    .execute(this::attempt);
        }

        private void handleFailure(Throwable ex) {
            if (attempts.incrementAndGet() >= maxRetries) {
                resultFuture.completeExceptionally(ex);
            } else {
                long delayMs = calculateBackoff(attempts.get());
                CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS, executorService)
                        .execute(this::attempt);
            }
        }

        private long parseRetryAfter(Response response) {
            // 1. Check Retry-After header
            String retryAfter = response.getHeader("Retry-After");
            if (retryAfter != null) {
                try {
                    return TimeUnit.SECONDS.toMillis(Long.parseLong(retryAfter));
                } catch (NumberFormatException e) {
                    // Fall through to default backoff
                }
            }

            // 2. Default exponential backoff with jitter
            return calculateBackoff(attempts.get());
        }

        private long calculateBackoff(int attempt) {
            long baseDelay = (long) Math.min(500 * Math.pow(2, attempt), 30000); // Cap at 30s
            long jitter = ThreadLocalRandom.current().nextLong(baseDelay / 2);
            return baseDelay + jitter;
        }
    }


    public CompletableFuture<Response> executeWithRetry(Request request, int maxRetries) {
        CompletableFuture<Response> resultFuture = new CompletableFuture<>();
        new RetryHttpClient.RetryHandler(request, maxRetries, resultFuture).attempt();
        return resultFuture;
    }

    public HttpResponse<String> sendWithRetry(HttpRequest request) throws IOException, InterruptedException {
        int retryCount = 0;
        HttpResponse<String> response = null;
        IOException lastIoException = null;

        while (retryCount <= maxRetries) {
            try {
                response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                if (!shouldRetry.test(response) || retryCount == maxRetries) {
                    return response;
                }
            } catch (IOException e) {
                lastIoException = e;
                if (retryCount == maxRetries) {
                    throw e;
                }
            }

            retryCount++;
            long delay = response.statusCode() == 429
                    ? parseRetryAfter(response,retryCount)
                    : calculateBackoff(retryCount);
            long jitter = ThreadLocalRandom.current().nextLong(delay / 2);
            delay += jitter;

            if (retryCount <= maxRetries) {
                LOGGER.info(String.format("attempting %d/%d retries to %s after %dms",retryCount,maxRetries,request.uri().toString(),delay));
                Thread.sleep(delay);
            }
        }

        if (lastIoException != null) {
            throw lastIoException;
        }
        return response;
    }


    private long calculateBackoff(int attempt) {
        return (long) (retryDelayMillis * Math.pow(backoffFactor, attempt - 1));
    }

    private long parseRetryAfter(HttpResponse<String> response, int retryCount) {
        return response.headers()
                .firstValueAsLong("Retry-After")
                .orElse(calculateBackoff(retryCount))*500; // Convert seconds to milliseconds
    }

    private static Predicate<HttpResponse<String>> defaultRetryCondition() {
        return response -> {
            int statusCode = response.statusCode();
            return statusCode >= 500 || statusCode == 429; // Server errors or too many requests
        };
    }

    public static class RetryLimitExceededException extends RuntimeException {
        public RetryLimitExceededException(int maxRetries) {
            super("Max retries (" + maxRetries + ") exceeded");
        }
    }
}