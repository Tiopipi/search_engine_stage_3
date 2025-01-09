package org.ulpgc.bd.service.implementation;

import org.ulpgc.bd.model.QueryResult;
import org.ulpgc.bd.service.interfaces.TransformerServiceInterface;

import java.util.*;

public class TransformerService implements TransformerServiceInterface {

    @Override
    public Map<String, Object> transformQueryResults(Map<String, QueryResult> results) {
        Map<String, Object> transformedResults = new HashMap<>();

        for (Map.Entry<String, QueryResult> entry : results.entrySet()) {
            String word = entry.getKey();
            QueryResult queryResult = entry.getValue();
            Map<String, Object> transformedQueryResult = new HashMap<>();
            transformedQueryResult.put("id", queryResult.getDocIds());
            transformedQueryResult.put("p", queryResult.getPositions());
            transformedQueryResult.put("f", queryResult.getFrequencies());
            transformedQueryResult.put("pa", queryResult.getParagraphs());
            transformedQueryResult.put("t", queryResult.getTitles());
            transformedResults.put(word, transformedQueryResult);
        }

        return transformedResults;
    }
}

