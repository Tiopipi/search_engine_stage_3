package org.ulpgc.bd.repository.implementation;

import com.hazelcast.collection.IQueue;
import org.example.model.Metadata;
import org.ulpgc.bd.repository.interfaces.MetadataLoader;
import org.ulpgc.bd.utils.batch.BatchProcessorInterface;
import org.ulpgc.bd.utils.hazelcast.HazelcastNode;
import com.hazelcast.map.IMap;
import java.util.*;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CsvMetadataLoader implements MetadataLoader {


    private static final Logger LOGGER = Logger.getLogger(MetadataLoader.class.getName());
    private final HazelcastNode hazelcastNode;
    private BatchProcessorInterface batchProcessor;

    public CsvMetadataLoader(HazelcastNode hazelcastNode, BatchProcessorInterface batchProcessor) {
        this.hazelcastNode = hazelcastNode;
        this.batchProcessor = batchProcessor;
    }

    @Override
    public void loadMetadata(String metadataPath) {
        IQueue<String> metadataQueue = hazelcastNode.getMetadataLoadedSet();
        IMap<String, List<String>> metadataMap = hazelcastNode.getMetadataIMap();

        LOGGER.info("Starting distributed metadata loading. Total entries in MetadataQueue: " + metadataQueue.size());

        List<String> batch = new ArrayList<>(200);
        Map<String, List<String>> localMetadataMap = new HashMap<>();

        while (true) {
            int drainedSize = metadataQueue.drainTo(batch, 200);
            if (drainedSize == 0) {
                break;
            }

            try {
                batchProcessor.processBatchMetadata(batch, metadataPath, localMetadataMap);

                metadataMap.putAll(localMetadataMap);
                localMetadataMap.clear();

                LOGGER.info("Processed batch of " + drainedSize + " documents.");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing batch", e);
            }

            batch.clear();
        }

        LOGGER.info("Metadata loading completed.");
    }


    @Override
    public List<Metadata> convertIMapToMetadataList(IMap<String, List<String>> metadataMap) {
        List<Metadata> metadataList = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : metadataMap.entrySet()) {
            String id = entry.getKey();
            List<String> attributes = entry.getValue();

            if (attributes.size() >= 4) {
                String title = attributes.get(0);
                String author = attributes.get(1);
                String releaseDate = attributes.get(2);
                String language = attributes.get(3);

                Metadata metadata = new Metadata(title, author, releaseDate, language, id);
                metadataList.add(metadata);
            } else {
                System.err.println("Insufficient attributes for Metadata ID: " + id);
            }
        }

        return metadataList;
    }

}
