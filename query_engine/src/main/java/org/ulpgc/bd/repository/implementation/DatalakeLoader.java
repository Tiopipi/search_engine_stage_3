package org.ulpgc.bd.repository.implementation;

import com.hazelcast.collection.IQueue;
import com.hazelcast.map.IMap;
import org.ulpgc.bd.repository.interfaces.DatalakeLoaderInterface;
import org.ulpgc.bd.utils.batch.BatchProcessorInterface;
import org.ulpgc.bd.utils.hazelcast.HazelcastNode;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class DatalakeLoader implements DatalakeLoaderInterface {

    private static final Logger LOGGER = Logger.getLogger(DatalakeLoader.class.getName());
    private final String datalakePath;
    private final HazelcastNode hazelcastNode;
    private BatchProcessorInterface batchProcessor;

    public DatalakeLoader(String datalakePath, HazelcastNode hazelcastNode, BatchProcessorInterface batchProcessor) {
        this.datalakePath = datalakePath;
        this.hazelcastNode = hazelcastNode;
        this.batchProcessor = batchProcessor;
    }

    @Override
    public void loadDatalake() {
        IQueue<String> datalakeQueue = hazelcastNode.getDatalakeLoadedSet();
        IMap<String, String> datalakeIMap = hazelcastNode.getDatalakeMap();
        LOGGER.info("Starting sequential datalake loading. Total entries in DatalakeCacheQueue: " + datalakeQueue.size());
        List<String> batch = new ArrayList<>(500);
        Map<String, String> datalakeMap = new HashMap<>();

        while (true) {
            int drainedSize = datalakeQueue.drainTo(batch, 500);
            if (drainedSize == 0) {
                break;
            }

            try {
                batchProcessor.processBatchDatalake(batch, datalakeMap);
                datalakeIMap.putAll(datalakeMap);
                datalakeMap.clear();

                LOGGER.info("Processed batch of " + drainedSize + " books");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing batch", e);
            }

            batch.clear();
        }

        LOGGER.info("Datalake loading completed.");
    }

    @Override
    public Stream<Path> loadBooksPath() throws IOException {
        return Files.list(Path.of(datalakePath));
    }
}
