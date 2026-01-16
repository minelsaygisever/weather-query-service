package com.minelsaygisever.weatherqueryservice.service.aggregator;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class WeatherAggregatorService {

    private final WeatherService weatherService;

    // Key: Location (ex: "Istanbul")
    // Value: Batch
    private final Map<String, Batch> activeBatches = new ConcurrentHashMap<>();

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    private static final int BATCH_SIZE = 10;
    private static final long BATCH_TIMEOUT_MS = 5000; // 5 seconds

    public CompletableFuture<WeatherResponse> getWeather(String location) {
        CompletableFuture<WeatherResponse> future = new CompletableFuture<>();

        activeBatches.compute(location, (key, existingBatch) -> {
            if (existingBatch != null && (existingBatch.isFull() || existingBatch.isProcessed.get())) {
                processBatch(existingBatch);
                existingBatch = null;
            }

            if (existingBatch == null) {
                existingBatch = new Batch(location);

                Batch finalBatchRef = existingBatch;
                existingBatch.timeoutTask = scheduler.schedule(
                        () -> processBatch(finalBatchRef),
                        BATCH_TIMEOUT_MS,
                        TimeUnit.MILLISECONDS
                );
                log.debug("[{}] A new batch has been created.", location);
            }

            existingBatch.addClient(future);

            if (existingBatch.isFull()) {
                log.info("[{}] Batch limit (10) has been reached. It is being shipped immediately.", location);
                processBatch(existingBatch);
            }

            return existingBatch;
        });

        return future;
    }

    private void processBatch(Batch batch) {
        if (!batch.isProcessed.compareAndSet(false, true)) {
            return;
        }

        activeBatches.remove(batch.location, batch);
        batch.cancelTimer();

        log.info("Batch is being processed: Location={}, ClientCount={}", batch.location, batch.waitingClients.size());
        CompletableFuture.runAsync(() -> {
            try {
                int currentRequestCount = batch.waitingClients.size();

                WeatherResponse response = weatherService.getWeather(batch.location, currentRequestCount);

                for (CompletableFuture<WeatherResponse> client : batch.waitingClients) {
                    client.complete(response);
                }
            } catch (Exception e) {
                log.error("An error occurred while processing the batch: {}", e.getMessage());
                for (CompletableFuture<WeatherResponse> client : batch.waitingClients) {
                    client.completeExceptionally(e);
                }
            }
        });
    }

    private class Batch {
        final String location;
        final List<CompletableFuture<WeatherResponse>> waitingClients = new ArrayList<>();
        ScheduledFuture<?> timeoutTask;
        final AtomicBoolean isProcessed = new AtomicBoolean(false);

        Batch(String location) {
            this.location = location;
        }

        void addClient(CompletableFuture<WeatherResponse> future) {
            waitingClients.add(future);
        }

        boolean isFull() {
            return waitingClients.size() >= BATCH_SIZE;
        }

        void cancelTimer() {
            if (timeoutTask != null && !timeoutTask.isDone()) {
                timeoutTask.cancel(false);
            }
        }
    }

    @jakarta.annotation.PreDestroy
    public void stopScheduler() {
        scheduler.shutdown();
    }
}
