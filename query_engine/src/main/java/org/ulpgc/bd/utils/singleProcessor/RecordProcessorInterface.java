package org.ulpgc.bd.utils.singleProcessor;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface RecordProcessorInterface {
    void processSingleMetadata(String documentId, String metadataPath, Map<String, List<String>> localMetadataMap) throws IOException;
    void processSingleBook(String bookId, Map<String, String> datalakeMap) throws IOException;
    void processSingleWord(String word, ConcurrentHashMap<String, String> datamartMap);
}
