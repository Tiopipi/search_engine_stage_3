package org.ulpgc.bd.utils.invertedIndex;

import org.example.model.*;

import java.util.*;

public class InvertedIndexFilterUtils {

    private InvertedIndexFilterUtils() {
    }

    public static Set<String> extractCommonIds(
            Map<String, InvertedIndex> results,
            String[] queryWords
    ) {
        Set<String> commonIds = null;

        for (String word : queryWords) {
            InvertedIndex data = results.get(word);
            if (data != null) {
                if (commonIds == null) {
                    commonIds = new HashSet<>(data.getDocIds());
                } else {
                    commonIds.retainAll(data.getDocIds());
                }
            }
        }
        return commonIds;
    }

    public static void filterByCommonIds(Map<String, InvertedIndex> results, Set<String> commonIds) {
        if (commonIds == null || commonIds.isEmpty()) {
            results.clear();
            return;
        }

        results.replaceAll((word, data) -> filterData(data, commonIds));
    }



    private static InvertedIndex filterData(InvertedIndex data, Set<String> commonIds) {
        List<String> filteredIds = new ArrayList<>();
        List<List<Integer>> filteredPositions = new ArrayList<>();
        List<Integer> filteredFrequencies = new ArrayList<>();

        for (int i = 0; i < data.getDocIds().size(); i++) {
            if (commonIds.contains(data.getDocIds().get(i))) {
                filteredIds.add(data.getDocIds().get(i));
                filteredPositions.add(data.getPositions().get(i));
                filteredFrequencies.add(data.getFrequencies().get(i));
            }
        }

        data.setDocIds(filteredIds);
        data.setPositions(filteredPositions);
        data.setFrequencies(filteredFrequencies);

        return data;
    }

    public static void filterByDocumentMap(
            Map<String, InvertedIndex> results,
            Map<String, Document> documentMap
    ) {
        results.replaceAll((word, data) -> filterByExistingDocuments(data, documentMap));
    }

    private static InvertedIndex filterByExistingDocuments(
            InvertedIndex data,
            Map<String, Document> documentMap
    ) {
        List<String> filteredIds = new ArrayList<>();
        List<List<Integer>> filteredPositions = new ArrayList<>();
        List<Integer> filteredFrequencies = new ArrayList<>();

        for (int i = 0; i < data.getDocIds().size(); i++) {
            if (documentMap.containsKey(data.getDocIds().get(i))) {
                filteredIds.add(data.getDocIds().get(i));
                filteredPositions.add(data.getPositions().get(i));
                filteredFrequencies.add(data.getFrequencies().get(i));
            }
        }
        data.setDocIds(filteredIds);
        data.setPositions(filteredPositions);
        data.setFrequencies(filteredFrequencies);

        return data;
    }
}