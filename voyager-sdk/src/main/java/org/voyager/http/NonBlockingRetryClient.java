package org.voyager.http;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Request;
import org.asynchttpclient.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class NonBlockingRetryClient {
    private final AsyncHttpClient asyncHttpClient;
    private final Logger LOGGER = LoggerFactory.getLogger(NonBlockingRetryClient.class);
    private ExecutorService executorService;

    public NonBlockingRetryClient(ExecutorService executorService) {
        this.asyncHttpClient = Dsl.asyncHttpClient();
        this.executorService = executorService;
    }

    public CompletableFuture<Response> executeWithRetry(Request request, int maxRetries) {
        CompletableFuture<Response> resultFuture = new CompletableFuture<>();
        new RetryHandler(request, maxRetries, resultFuture).attempt();
        return resultFuture;
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
            if (attempts.get() >= maxRetries) {
                resultFuture.completeExceptionally(new RetryLimitExceededException(maxRetries));
                return;
            }

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

    public static class RetryLimitExceededException extends RuntimeException {
        public RetryLimitExceededException(int maxRetries) {
            super("Max retries (" + maxRetries + ") exceeded");
        }
    }

    public void shutdown() throws IOException {
        asyncHttpClient.close();
    }
}
