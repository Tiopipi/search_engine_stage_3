package org.ulpgc.bd.control;

import org.example.model.Document;
import org.example.model.InvertedIndex;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface InvertedIndexManager {

    Map<String, InvertedIndex> buildInvertedIndexWithPositions(List<Document> documents, Set<String> stopWords, Set<String> processedBooks, String indexDirectory);

    void updateInvertedIndex(Map<String, InvertedIndex> newEntries, String baseDirectory);

    void export(Map<String, InvertedIndex> invertedIndex, String baseDirectory);

}
