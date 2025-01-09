package org.ulpgc.bd.control;

import org.example.model.*;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.ulpgc.bd.service.interfaces.QueryProcessorInterface;
import org.ulpgc.bd.utils.cache.CacheManagerInterface;
import org.ulpgc.bd.utils.metadata.MetadataFilterInterface;
import org.ulpgc.bd.utils.stats.StatsGeneratorInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class QueryService {
    private final QueryProcessorInterface queryProcessor;
    private final CacheManagerInterface cacheManager;
    private final MetadataFilterInterface metadataFilter;
    private final StatsGeneratorInterface statsGenerator;
    private final InvertedIndexLoader indexLoader;

    public QueryService(
            QueryProcessorInterface queryProcessor,
            CacheManagerInterface cacheManager,
            MetadataFilterInterface metadataFilter,
            StatsGeneratorInterface statsGenerator, InvertedIndexLoader indexLoader) {
        this.queryProcessor = queryProcessor;
        this.cacheManager = cacheManager;
        this.metadataFilter = metadataFilter;
        this.statsGenerator = statsGenerator;
        this.indexLoader = indexLoader;
    }

    public Map<String, Object> processQueryHazelcast(Map<String, String> params) {
        List<Metadata> filteredMetadata = getFilteredMetadata(params);
        Map<String, String[]> queryResponse = cacheManager.getInvertedIndex(params.get("query"));
        Map<String, Object> response = queryProcessor.executeQueryHazelcast(params.get("query"), filteredMetadata, queryResponse);
        statsGenerator.updateSearchFrequency(params.get("query"));

        return response;
    }

    public Map<String, Object> processQuery(Map<String, String> params) {
        List<Metadata> filteredMetadata = getFilteredMetadata(params);
        Map<String, InvertedIndex> queryResponse = new HashMap<>();

        for (String word : params.get("query").toLowerCase().split(" ")){
            InvertedIndex invertedIndex = indexLoader.loadInvertedIndex(word);
            if (invertedIndex != null && !invertedIndex.getDocIds().isEmpty()) {
                queryResponse.put(word, invertedIndex);
            }
        }

        Map<String, Object> response = queryProcessor.executeQuery(params.get("query"), filteredMetadata, queryResponse);
        statsGenerator.updateSearchFrequency(params.get("query"));

        return response;
    }

    private List<Metadata> getFilteredMetadata(Map<String, String> params) {
        return metadataFilter.getFilteredAndDateRangeMetadata(params);
    }
}
