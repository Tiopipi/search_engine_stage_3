package org.ulpgc.bd.control;

import spark.Request;
import spark.Response;
import spark.Route;

import java.util.*;

public class QueryController {

    private QueryParameterExtractor queryParameterExtractor;
    private QueryResponseHandler queryResponseHandler;

    public QueryController(QueryParameterExtractor queryParameterExtractor,QueryResponseHandler queryResponseHandler) {
        this.queryParameterExtractor = queryParameterExtractor;
        this.queryResponseHandler = queryResponseHandler;
    }

    public Route searchClientHazelcast = (Request req, Response res) -> {
        String combinedQuery = queryParameterExtractor.extractCombinedQuery(req);
        Map<String, String> params = queryParameterExtractor.extractParams(req, combinedQuery);
        return queryResponseHandler.handleQueryResponseHazelcast(res, params);
    };

    public Route searchClient = (Request req, Response res) -> {
        String combinedQuery = queryParameterExtractor.extractCombinedQuery(req);
        Map<String, String> params = queryParameterExtractor.extractParams(req, combinedQuery);
        return queryResponseHandler.handleQueryResponse(res, params);
    };

    public Route searchStats = (Request req, Response res) -> {
        String type = req.params("type");
        if (queryResponseHandler.isInvalidType(res, type)) return "Invalid type";
        return queryResponseHandler.handleStatsResponse(req, res, type);
    };
}