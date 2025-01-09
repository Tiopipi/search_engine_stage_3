package org.ulpgc.bd.utils;

import java.util.Map;
import org.example.model.InvertedIndex;

public class MapUtils {

    private MapUtils() {
    }

    public static void mergeInvertedIndexData(Map<String, InvertedIndex> existingData, Map<String, InvertedIndex> newData) {
        newData.forEach((key, value) ->
                existingData.merge(key, value, (existing, toMerge) -> {
                    existing.mergeWith(toMerge);
                    return existing;
                })
        );
    }
}
