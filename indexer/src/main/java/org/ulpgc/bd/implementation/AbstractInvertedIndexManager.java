package org.ulpgc.bd.implementation;

import org.apache.lucene.analysis.Analyzer;
import org.example.model.Document;
import org.ulpgc.bd.control.InvertedIndexManager;
import org.ulpgc.bd.utils.TextUtils;
import org.example.model.InvertedIndex;
import java.util.*;

public abstract class AbstractInvertedIndexManager implements InvertedIndexManager {
    protected final Analyzer analyzer;

    protected AbstractInvertedIndexManager(Analyzer analyzer) {
        this.analyzer = analyzer;
    }

    @Override
    public Map<String, InvertedIndex> buildInvertedIndexWithPositions(
            List<Document> documents, Set<String> stopWords, Set<String> processedBooks, String indexDirectory) {
        Map<String, InvertedIndex> index = new HashMap<>();
        for (Document document : documents) {
            if (!processedBooks.contains(document.getId())) {
                processDocument(document, stopWords, index);
            }
        }
        return index;
    }

    protected void processDocument(Document document, Set<String> stopWords, Map<String, InvertedIndex> index) {
        String[] words = document.getContent().split("\\s+");
        for (int position = 0; position < words.length; position++) {
            processWord(document.getId(), words[position], position, stopWords, index);
        }
    }

    protected void processWord(String documentId, String word, int position, Set<String> stopWords, Map<String, InvertedIndex> index) {
        String cleanedDocId = documentId.replaceAll("\\.txt$", "");

        List<String> terms = TextUtils.cleanAndNormalize(word, analyzer);
        for (String term : terms) {
            if (!stopWords.contains(term)) {
                index.computeIfAbsent(term, k -> new InvertedIndex()).addPosition(cleanedDocId, position);
            }
        }
    }
}
