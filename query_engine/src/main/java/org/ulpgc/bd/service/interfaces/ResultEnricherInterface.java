package org.ulpgc.bd.service.interfaces;

import org.example.model.*;
import org.ulpgc.bd.model.QueryResult;

import java.util.Map;

public interface ResultEnricherInterface {
    QueryResult enrich(InvertedIndex invertedIndex, Map<String, Document> documentMap);
}
