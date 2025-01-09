package org.ulpgc.bd.control;

import com.fasterxml.jackson.databind.ObjectMapper;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.Map;

public class QueryResponseHandler {

    private final QueryService queryService;
    private final StatsService statsService;
    private final QueryParameterExtractor queryParameterExtractor;

    public QueryResponseHandler(QueryService queryService, StatsService statsService, QueryParameterExtractor queryParameterExtractor){
        this.queryService = queryService;
        this.statsService =statsService;
        this.queryParameterExtractor = queryParameterExtractor;
    }
    public Object handleQueryResponseHazelcast(Response res, Map<String, String> params) throws IOException {
        Map<String, Object> queryResults = queryService.processQueryHazelcast(params);
        res.type("application/json");
        System.out.println("Query results returned");
        return new ObjectMapper().writeValueAsString(queryResults);
    }

    public Object handleQueryResponse(Response res, Map<String, String> params) throws IOException {
        Map<String, Object> queryResults = queryService.processQuery(params);
        res.type("application/json");
        return new ObjectMapper().writeValueAsString(queryResults);
    }

    public boolean isInvalidType(Response res, String type) {
        if (type == null || type.trim().isEmpty()) {
            res.status(400);
            return true;
        }
        return false;
    }

    public Object handleStatsResponse(Request req, Response res, String type) throws IOException {
        Map<String, String> params = queryParameterExtractor.extractRequestParams(req);
        Map<String, Object> stats = statsService.calculateStats(type, params);

        if (stats == null || stats.isEmpty()) {
            res.status(400);
            return "Please, enter a valid stat";
        }

        res.type("application/json");
        return stats;
    }
}