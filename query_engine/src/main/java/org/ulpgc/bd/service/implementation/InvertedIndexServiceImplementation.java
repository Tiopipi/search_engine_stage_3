package org.ulpgc.bd.service.implementation;

import org.example.model.*;
import org.ulpgc.bd.model.QueryResult;
import org.ulpgc.bd.service.interfaces.*;
import org.ulpgc.bd.utils.invertedIndex.InvertedIndexFilterUtils;

import java.util.Map.Entry;
import java.util.*;
import java.util.stream.Collectors;


public class InvertedIndexServiceImplementation implements InvertedIndexService {

    private final ResultEnricherInterface resultEnricher;

    public InvertedIndexServiceImplementation(ResultEnricherInterface resultEnricher) {
        this.resultEnricher = resultEnricher;
    }

    @Override
    public Map<String, QueryResult> searchInvertedIndex(
            String query,
            Map<String, InvertedIndex> loadedIndexes,
            Map<String, Document> documentMap) {

        validateQuery(query);

        Map<String, InvertedIndex> queryResults = initializeResults(query, loadedIndexes);

        filterResults(query, queryResults, documentMap);

        return convertToQueryResults(queryResults, documentMap);
    }

    @Override
    public Map<String, QueryResult> searchInvertedIndexHazelcast(
            String query,
            Map<String, String[]> serializedIndexes,
            Map<String, Document> documentMap) {

        if (serializedIndexes.isEmpty()) {
            System.out.println("Query result is empty in Hazelcast cache for query: " + query);
            return Collections.emptyMap();
        }

        Map<String, InvertedIndex> deserializedIndexes = serializedIndexes.entrySet().stream()
                .collect(Collectors.toMap(
                        Entry::getKey,
                        entry -> deserializeAttributes(entry.getValue())
                ));

        return searchInvertedIndex(query, deserializedIndexes, documentMap);
    }


    private void validateQuery(String query) {
        if (query == null || query.isEmpty()) {
            throw new IllegalArgumentException("No search query provided");
        }
    }

    private Map<String, InvertedIndex> initializeResults(
            String query,
            Map<String, InvertedIndex> loadedIndexes) {

        return Arrays.stream(query.split("\\s+"))
                .filter(loadedIndexes::containsKey)
                .collect(Collectors.toMap(word -> word, loadedIndexes::get));
    }

    private void filterResults(String query, Map<String, InvertedIndex> results, Map<String, Document> documentMap) {
        Set<String> commonIds = InvertedIndexFilterUtils.extractCommonIds(results, query.split("\\s+"));
        if (commonIds == null || commonIds.isEmpty()) {
            results.clear();
            return;
        }
        InvertedIndexFilterUtils.filterByCommonIds(results, commonIds);
        InvertedIndexFilterUtils.filterByDocumentMap(results, documentMap);
    }

    private Map<String, QueryResult> convertToQueryResults(
            Map<String, InvertedIndex> queryResults,
            Map<String, Document> documentMap) {

        return queryResults.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> resultEnricher.enrich(entry.getValue(), documentMap)
                ));
    }

    private InvertedIndex deserializeAttributes(String[] attributes) {
        if (attributes.length != 3) {
            throw new IllegalArgumentException("Invalid attributes format");
        }

        InvertedIndex invertedIndex = new InvertedIndex();
        invertedIndex.setDocIds(parseStringList(attributes[0]));
        invertedIndex.setPositions(parseNestedIntegerList(attributes[1]));
        invertedIndex.setFrequencies(parseIntegerList(attributes[2]));

        return invertedIndex;
    }

    private List<String> parseStringList(String data) {
        data = data.replace("[", "").replace("]", "");
        if (data.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(data.split(",\\s*"));
    }

    private List<Integer> parseIntegerList(String data) {
        data = data.replace("[", "").replace("]", "");
        if (data.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.stream(data.split(",\\s*"))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private List<List<Integer>> parseNestedIntegerList(String data) {
        data = data.replace(" ", "").replace("],[", "]~[");
        data = data.substring(1, data.length() - 1);
        String[] nestedLists = data.split("~");

        List<List<Integer>> result = new ArrayList<>();
        for (String nested : nestedLists) {
            result.add(parseIntegerList(nested));
        }
        return result;
    }
}
