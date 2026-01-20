package com.minelsaygisever.weatherqueryservice.service.aggregator;

import com.minelsaygisever.weatherqueryservice.model.dto.WeatherResponse;
import com.minelsaygisever.weatherqueryservice.service.WeatherService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
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

    private final ExecutorService ioExecutor = Executors.newFixedThreadPool(50);

    @Value("${weather.aggregator.batch-size:10}")
    private static final int BATCH_SIZE = 10;
    @Value("${weather.aggregator.batch-timeout-ms:5000}")
    private static final long BATCH_TIMEOUT_MS = 5000;

    public CompletableFuture<WeatherResponse> getWeather(String location) {
        CompletableFuture<WeatherResponse> future = new CompletableFuture<>();

        // Atomic operation
        activeBatches.compute(location, (key, batch) -> {
            if (batch != null) {
                if (batch.isFull() && !batch.isProcessed.get()) {
                    processBatch(batch);
                    batch = null;
                }
                else if ( batch.isProcessed.get()) {
                    batch = null;
                }
            }

            if (batch == null) {
                batch = createBatchWithTimeout(location);
            }

            boolean added = batch.tryAddClient(future);
            if (!added) {
                Batch newBatch = createBatchWithTimeout(location);
                newBatch.addClient(future);
                return newBatch;
            }

            // If batch limit is reached, process immediately
            if (batch.isFull()) {
                log.info("[{}] Batch limit (10) has been reached. It is being shipped immediately.", location);
                processBatch(batch);
                return null; // Remove from map
            }

            return batch; // Update map with current batch
        });

        return future;
    }

    private void processBatch(Batch batch) {
        List<CompletableFuture<WeatherResponse>> clientsSnapshot;

        synchronized (batch.waitingClients) {
            // Ensure batch is processed exactly once
            if (!batch.isProcessed.compareAndSet(false, true)) {
                return;
            }
            clientsSnapshot = new ArrayList<>(batch.waitingClients);
        }

        batch.cancelTimer();
        activeBatches.remove(batch.location, batch);

        ioExecutor.execute(() -> {
            try {
                WeatherResponse response = weatherService.getWeather(batch.location, clientsSnapshot.size());
                // Distribute result to all waiting clients
                clientsSnapshot.forEach(cf -> cf.complete(response));
            } catch (Exception e) {
                clientsSnapshot.forEach(cf -> cf.completeExceptionally(e));
            }
        });
    }

    private Batch createBatchWithTimeout(String location) {
        Batch batch = new Batch(location);
        Batch ref = batch;

        // Schedule automatic processing after 5 seconds
        batch.timeoutTask = scheduler.schedule(
                () -> processBatch(ref),
                BATCH_TIMEOUT_MS,
                TimeUnit.MILLISECONDS
        );

        log.debug("[{}] A new batch has been created.", location);
        return batch;
    }

    private class Batch {
        final String location;
        final List<CompletableFuture<WeatherResponse>> waitingClients = Collections.synchronizedList(new ArrayList<>());
        ScheduledFuture<?> timeoutTask;
        final AtomicBoolean isProcessed = new AtomicBoolean(false);

        Batch(String location) {
            this.location = location;
        }

        void addClient(CompletableFuture<WeatherResponse> future) {
            waitingClients.add(future);
        }

        boolean tryAddClient(CompletableFuture<WeatherResponse> future) {
            synchronized (waitingClients) {
                if (isProcessed.get() || waitingClients.size() >= BATCH_SIZE) {
                    return false;
                }
                waitingClients.add(future);
                return true;
            }
        }

        boolean isFull() {
            synchronized (waitingClients) {
                return waitingClients.size() >= BATCH_SIZE;
            }
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
        ioExecutor.shutdown();
    }
}
