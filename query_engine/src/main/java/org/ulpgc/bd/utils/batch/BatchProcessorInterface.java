package org.ulpgc.bd.utils.batch;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public interface BatchProcessorInterface {
    void processBatchMetadata(List<String> batch, String metadataPath, Map<String, List<String>> localMetadataMap);
    void processBatchDatalake(List<String> batch, Map<String, String> datalakeMap);
    void processBatchConcurrently(List<String> batch, ConcurrentHashMap<String, String> datamartMap, ExecutorService executorService);
}
