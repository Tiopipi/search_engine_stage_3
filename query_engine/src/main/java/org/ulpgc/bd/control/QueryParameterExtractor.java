package org.ulpgc.bd.control;

import spark.Request;

import java.util.*;

public class QueryParameterExtractor {

    public String extractCombinedQuery(Request req) {
        List<String> words = new ArrayList<>();
        extractWordsFromParam(req.params(":words"), words);
        extractWordsFromParam(req.queryParams("query"), words);
        return String.join(" ", words);
    }

    public void extractWordsFromParam(String param, List<String> words) {
        if (param != null && !param.trim().isEmpty()) {
            String decoded = param.replace("+", " ").trim().toLowerCase();
            words.addAll(Arrays.asList(decoded.split("\\s+")));
        }
    }

    public Map<String, String> extractParams(Request req, String combinedQuery) {
        return Map.of(
                "query", combinedQuery,
                "title", Optional.ofNullable(req.queryParams("title")).orElse(""),
                "author", Optional.ofNullable(req.queryParams("author")).orElse(""),
                "year", Optional.ofNullable(req.queryParams("year")).orElse(""),
                "month", Optional.ofNullable(req.queryParams("month")).orElse(""),
                "day", Optional.ofNullable(req.queryParams("day")).orElse(""),
                "language", Optional.ofNullable(req.queryParams("language")).orElse(""),
                "from", Optional.ofNullable(req.queryParams("from")).orElse(""),
                "to", Optional.ofNullable(req.queryParams("to")).orElse("")
        );
    }

    public Map<String, String> extractRequestParams(Request req) {
        Map<String, String> params = new HashMap<>();
        req.queryParams().forEach(param -> params.put(param, req.queryParams(param)));
        return params;
    }
}