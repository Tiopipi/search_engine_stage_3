package org.ulpgc.bd.service.interfaces;

import org.example.model.*;
import org.ulpgc.bd.model.QueryResult;
import java.util.Map;


public interface InvertedIndexService {
    Map<String, QueryResult> searchInvertedIndex(String query, Map<String, InvertedIndex> loadedIndexes, Map<String, Document> documentMap);
    Map<String, QueryResult> searchInvertedIndexHazelcast(
            String query,
            Map<String, String[]> serializedIndexes,
            Map<String, Document> documentMap);
}
