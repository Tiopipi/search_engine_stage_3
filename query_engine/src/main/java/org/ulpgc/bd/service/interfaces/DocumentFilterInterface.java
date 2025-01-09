package org.ulpgc.bd.service.interfaces;

import org.example.model.*;
import java.util.*;

public interface DocumentFilterInterface {
    Map<String, Document> filterDocumentsHazelcast(Map<String, String[]> loadedIndexes, List<Metadata> filteredMetadata);
    Map<String, Document> filterDocuments(Map<String, InvertedIndex> loadedIndexes, List<Metadata> filteredMetadata);
}
