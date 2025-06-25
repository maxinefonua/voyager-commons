//package org.voyager.http;
//
//import org.asynchttpclient.AsyncHttpClient;
//import org.asynchttpclient.Dsl;
//import org.asynchttpclient.Request;
//import org.asynchttpclient.Response;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.util.concurrent.CompletableFuture;
//import java.util.concurrent.ExecutorService;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.TimeUnit;
//import java.util.concurrent.atomic.AtomicInteger;
//
//public class RetryableAsyncHttpClient {
//    private final AsyncHttpClient asyncHttpClient;
//    private final ExecutorService callbackExecutor;
//    private final int maxRetries;
//    private final long initialBackoffMs;
//    private final double backoffMultiplier;
//    private final Logger LOGGER = LoggerFactory.getLogger(RetryableAsyncHttpClient.class);
//
//    public RetryableAsyncHttpClient(ExecutorService callbackExecutor,
//                                    int maxRetries,
//                                    long initialBackoffMs,
//                                    double backoffMultiplier) {
//        this.asyncHttpClient = Dsl.asyncHttpClient();
//        this.callbackExecutor = callbackExecutor;
//        this.maxRetries = maxRetries;
//        this.initialBackoffMs = initialBackoffMs;
//        this.backoffMultiplier = backoffMultiplier;
//    }
//
//    public CompletableFuture<Response> executeWithRetry(Request request) {
//        CompletableFuture<Response> resultFuture = new CompletableFuture<>();
//        executeWithRetry(request, new AtomicInteger(0), resultFuture);
//        return resultFuture;
//    }
//
//    private void executeWithRetry(Request request,
//                                  AtomicInteger retryCount,
//                                  CompletableFuture<Response> resultFuture) {
//        asyncHttpClient.executeRequest(request)
//                .toCompletableFuture()
//                .whenCompleteAsync((response, ex) -> {
//                    if (ex != null) {
//                        handleFailure(request, retryCount, resultFuture, ex);
//                    } else if (response.getStatusCode() == 429 && retryCount.get() < maxRetries) {
//                        handleRateLimit(request, retryCount, resultFuture, response);
//                    } else {
//                        resultFuture.complete(response);
//                    }
//                }, callbackExecutor);
//    }
//
//    private void handleRateLimit(Request request,
//                                 AtomicInteger retryCount,
//                                 CompletableFuture<Response> resultFuture,
//                                 Response response) {
//        long delay = getRetryAfterDelay(response,retryCount.incrementAndGet());
//        LOGGER.info(String.format("Rate limited. Retry %d/%d in %dms%n",
//                retryCount.get(), maxRetries, delay));
//        scheduleRetry(request, retryCount, resultFuture, delay);
//    }
//
//    private void handleFailure(Request request,
//                               AtomicInteger retryCount,
//                               CompletableFuture<Response> resultFuture,
//                               Throwable ex) {
//        if (retryCount.get() < maxRetries) {
//            long delay = calculateBackoff(retryCount.incrementAndGet());
//            System.err.printf("Request failed. Retry %d/%d in %dms (%s)%n",
//                    retryCount.get(), maxRetries, delay, ex.getMessage());
//
//            scheduleRetry(request, retryCount, resultFuture, delay);
//        } else {
//            resultFuture.completeExceptionally(ex);
//        }
//    }
//
//    private long getRetryAfterDelay(Response response,int attempt) {
//        String retryAfter = response.getHeader("Retry-After");
//        if (retryAfter != null) {
//            try {
//                return TimeUnit.SECONDS.toMillis(Integer.parseInt(retryAfter));
//            } catch (NumberFormatException e) {
//                // Fall back to calculated backoff
//            }
//        }
//        return calculateBackoff(attempt);
//    }
//
//    private long calculateBackoff(int attempt) {
//        long calculated = (long) (initialBackoffMs * Math.pow(backoffMultiplier, attempt - 1));
//        // Add ±20% jitter
//        double jitter = calculated * 0.2 * (ThreadLocalRandom.current().nextDouble() - 0.5);
//        return calculated + (long) jitter;
//    }
//
//    private void scheduleRetry(Request request,
//                               AtomicInteger retryCount,
//                               CompletableFuture<Response> resultFuture,
//                               long delayMs) {
//        CompletableFuture.delayedExecutor(delayMs, TimeUnit.MILLISECONDS, callbackExecutor)
//                .execute(() -> executeWithRetry(request, retryCount, resultFuture));
//    }
//
//    public void close() throws IOException {
//        asyncHttpClient.close();
//        callbackExecutor.shutdown();
//    }
//}