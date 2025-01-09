package org.ulpgc.bd.utils.cache;

import com.hazelcast.multimap.MultiMap;
import org.example.model.*;

import java.util.List;
import java.util.Map;

public interface CacheManagerInterface {
    Map<String, String[]> getInvertedIndex(String query);
    Map<String, Document> getDocument(String document);
}

