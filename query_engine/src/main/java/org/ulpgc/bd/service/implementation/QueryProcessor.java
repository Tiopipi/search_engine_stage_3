package org.ulpgc.bd.service.implementation;

import org.example.model.*;
import org.ulpgc.bd.model.QueryResult;
import org.ulpgc.bd.service.interfaces.*;

import java.util.*;

public class QueryProcessor implements QueryProcessorInterface {

    private final InvertedIndexService invertedIndexService;
    private final DocumentFilterInterface documentFilter;
    private final TransformerServiceInterface transformerService;

    public QueryProcessor(
            InvertedIndexService invertedIndexService,
            DocumentFilterInterface documentFilter,
            TransformerServiceInterface transformerService) {
        this.invertedIndexService = invertedIndexService;
        this.documentFilter = documentFilter;
        this.transformerService = transformerService;
    }

    @Override
    public Map<String, Object> executeQueryHazelcast(String query, List<Metadata> filteredMetadata, Map<String, String[]> queryResponse) {

        Map<String, Document> documentMap = documentFilter.filterDocumentsHazelcast(queryResponse, filteredMetadata);
        Map<String, QueryResult> results = invertedIndexService.searchInvertedIndexHazelcast(
                query.toLowerCase(), queryResponse, documentMap
        );
        return transformerService.transformQueryResults(results);
    }

    @Override
    public Map<String, Object> executeQuery(String query, List<Metadata> filteredMetadata, Map<String, InvertedIndex> queryResponse) {

        Map<String, Document> documentMap = documentFilter.filterDocuments(queryResponse, filteredMetadata);
        Map<String, QueryResult> results = invertedIndexService.searchInvertedIndex(
                query.toLowerCase(), queryResponse, documentMap
        );
        return transformerService.transformQueryResults(results);
    }
}
