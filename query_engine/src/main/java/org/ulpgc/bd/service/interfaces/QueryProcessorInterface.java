package org.ulpgc.bd.service.interfaces;

import org.example.model.*;
import java.util.*;

public interface QueryProcessorInterface {
    Map<String, Object> executeQueryHazelcast(String query, List<Metadata> filteredMetadata, Map<String, String[]> datamart);
    Map<String, Object> executeQuery(String query, List<Metadata> filteredMetadata, Map<String, InvertedIndex> datamart);
}
