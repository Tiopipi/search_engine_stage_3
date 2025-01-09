package org.ulpgc.bd.utils.stats;

import org.example.model.Metadata;
import org.ulpgc.bd.repository.interfaces.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class StatsGenerator implements StatsGeneratorInterface {
    private static ConcurrentHashMap<String, Integer> searchFrequency = new ConcurrentHashMap<>();
    private final StatUtils statUtils;

    public StatsGenerator(DatalakeLoaderInterface datalakeLoader, InvertedIndexLoader invertedIndexLoader, List<Metadata> metadataList) {
        this.statUtils = new StatUtils(invertedIndexLoader, datalakeLoader, metadataList);
    }

    @Override
    public Map<String, Object> generateStatistics(String type, Map<String, String> params) throws IOException {
        Map<String, Object> result = new HashMap<>();

        switch (type.toLowerCase()) {
            case "documents":
                result.put("documents", statUtils.countDocuments());
                break;
            case "authors":
                result.put("authors", statUtils.countAuthors());
                break;
            case "words":
                result.put("words", statUtils.countWords());
                break;
            case "languages":
                result.put("languages", statUtils.showLanguages());
                break;
            case "countbooksperyear":
                result.put("countbooksperyear", statUtils.countBooksPerYear());
                break;
            case "topauthor":
                result.put("topauthor", statUtils.topAuthor());
                break;
            case "olderbook":
                result.put("olderbook", statUtils.oldestBook());
                break;
            case "newerbook":
                result.put("newerbook", statUtils.newestBook());
                break;
            case "documentwithhighestfrequency":
                result.put("documentWithHighestFrequency",
                        statUtils.documentWithHighestFrequency(params.get("word")));
                break;
            case "searchstatistics":
                result.put("searchstatistics", statUtils.searchStatistics(searchFrequency));
                break;
            default:
                throw new IllegalArgumentException("Unsupported stat type: " + type);
        }

        return result;
    }

    @Override
    public void updateSearchFrequency(String query) {
        for (String word : query.toLowerCase().split("\\s+")) {
            searchFrequency.merge(word, 1, Integer::sum);
        }
    }
}
