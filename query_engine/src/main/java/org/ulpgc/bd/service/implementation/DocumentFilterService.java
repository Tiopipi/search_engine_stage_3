package org.ulpgc.bd.service.implementation;

import org.example.model.*;
import org.ulpgc.bd.repository.interfaces.DocumentLoader;
import org.ulpgc.bd.service.interfaces.DocumentFilterInterface;
import org.ulpgc.bd.utils.cache.CacheManagerInterface;

import java.util.*;

import java.util.stream.Collectors;

public class DocumentFilterService implements DocumentFilterInterface {
    private final CacheManagerInterface cacheManager;
    private final DocumentLoader documentLoader;

    public DocumentFilterService(CacheManagerInterface cacheManagerInterface, DocumentLoader documentLoader) {
        this.cacheManager = cacheManagerInterface;
        this.documentLoader = documentLoader;
    }

    @Override
    public Map<String, Document> filterDocumentsHazelcast(
            Map<String, String[]> queryResponse,
            List<Metadata> filteredMetadata) {

        Set<String> filteredDocumentIds = filteredMetadata.stream()
                .map(Metadata::getId)
                .collect(Collectors.toSet());

        Map<String, Document> documentMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : queryResponse.entrySet()) {
            String word = entry.getKey();
            String[] attributes = entry.getValue();

            List<String> docIds = parseStringList(attributes[0]);

            for (String docId : docIds) {
                if (filteredDocumentIds.contains(docId + ".txt")) {
                    documentMap.putAll(cacheManager.getDocument(docId));
                }
            }
        }
        return documentMap;
    }

    private List<String> parseStringList(String data) {
        data = data.replace("[", "").replace("]", "");
        if (data.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(data.split(",\\s*"));
    }

    @Override
    public Map<String, Document> filterDocuments(
            Map<String, InvertedIndex> loadedIndexes,
            List<Metadata> filteredMetadata) {
        Set<String> filteredDocumentIds = filteredMetadata.stream()
                .map(Metadata::getId)
                .collect(Collectors.toSet());

        Map<String, Document> documentMap = new HashMap<>();
        for (InvertedIndex index : loadedIndexes.values()) {

            for (String docId : index.getDocIds()) {
                if (filteredDocumentIds.contains(docId + ".txt")) {
                    Document document = documentLoader.loadDocument(docId);
                    if (document != null) {
                        documentMap.putIfAbsent(docId, document);
                    }
                }
            }
        }
        return documentMap;
    }
}
