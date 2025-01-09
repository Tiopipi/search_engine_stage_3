package org.ulpgc.bd.service.interfaces;

import org.ulpgc.bd.model.QueryResult;

import java.util.Map;

public interface TransformerServiceInterface {
    Map<String, Object> transformQueryResults(Map<String, QueryResult> results);
}