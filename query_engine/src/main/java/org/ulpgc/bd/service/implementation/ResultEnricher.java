package org.ulpgc.bd.service.implementation;

import org.example.model.*;
import org.ulpgc.bd.model.QueryResult;
import org.ulpgc.bd.service.interfaces.ResultEnricherInterface;
import org.ulpgc.bd.utils.document.DocumentUtils;

import java.util.*;

public class ResultEnricher implements ResultEnricherInterface {

    @Override
    public QueryResult enrich(InvertedIndex invertedIndex, Map<String, Document> documentMap) {
        QueryResult queryResult = new QueryResult();
        queryResult.setDocIds(invertedIndex.getDocIds());
        queryResult.setPositions(invertedIndex.getPositions());
        queryResult.setFrequencies(invertedIndex.getFrequencies());
        List<String> paragraphs = new ArrayList<>();
        List<String> titles = new ArrayList<>();

        for (String docId : invertedIndex.getDocIds()) {
            Document document = documentMap.get(docId);
            if (document != null) {
                titles.add(DocumentUtils.extractTitle(document));
                int firstPosition = invertedIndex.getPositions()
                        .get(invertedIndex.getDocIds().indexOf(docId)).get(0);
                paragraphs.add(DocumentUtils.extractParagraphByCharPosition(document, firstPosition));
            }
        }

        queryResult.setParagraphs(paragraphs);
        queryResult.setTitles(titles);
        return queryResult;
    }
}
