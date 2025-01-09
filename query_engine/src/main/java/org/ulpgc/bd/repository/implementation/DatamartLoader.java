package org.ulpgc.bd.repository.implementation;

import com.hazelcast.collection.IQueue;
import com.hazelcast.map.IMap;
import org.ulpgc.bd.repository.interfaces.DatamartLoaderInterface;
import org.ulpgc.bd.utils.batch.BatchProcessorInterface;
import org.ulpgc.bd.utils.hazelcast.HazelcastNode;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatamartLoader implements DatamartLoaderInterface {
    private static final Logger LOGGER = Logger.getLogger(DatamartLoader.class.getName());
    private final HazelcastNode hazelcastNode;
    private BatchProcessorInterface batchProcessor;

    public DatamartLoader(HazelcastNode hazelcastNode, BatchProcessorInterface batchProcessor) {
        this.hazelcastNode = hazelcastNode;
        this.batchProcessor = batchProcessor;
    }

    @Override
    public void loadDatamart() {
        IQueue<String> datamartQueue = hazelcastNode.getDatamartLoadedSet();
        IMap<String, String> datamartMultiMap = hazelcastNode.getDatamartMap();

        LOGGER.info("Starting distributed datamart loading.");

        List<String> batch = new ArrayList<>(50000);
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ConcurrentHashMap<String, String> datamartMap = new ConcurrentHashMap<>();

        try {
            loadAndProcessBatch(datamartQueue, batch, datamartMap, executorService, datamartMultiMap);
        } finally {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        LOGGER.info("Datamart loading completed.");
    }

    private void loadAndProcessBatch(IQueue<String> datamartQueue, List<String> batch,
                                     ConcurrentHashMap<String, String> datamartMap,
                                     ExecutorService executorService, IMap<String, String> datamartMultiMap) {
        while (true) {
            int drainedSize = datamartQueue.drainTo(batch, 50000);
            if (drainedSize == 0) {
                break;
            }

            try {
                batchProcessor.processBatchConcurrently(batch, datamartMap, executorService);
                datamartMultiMap.putAll(datamartMap);
                datamartMap.clear();
                LOGGER.info("Processed batch of " + drainedSize + " words");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing batch", e);
            }

            batch.clear();
        }
    }
}
