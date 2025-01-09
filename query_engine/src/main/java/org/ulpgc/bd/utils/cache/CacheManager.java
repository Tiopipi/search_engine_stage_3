package org.ulpgc.bd.utils.cache;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import org.example.model.Document;
import org.ulpgc.bd.utils.hazelcast.HazelcastNode;

import java.util.*;

public class CacheManager implements CacheManagerInterface {
    private final HazelcastInstance hazelcast;
    private IMap<Object, Object> datamartCache;
    private IMap<Object, Object> datalakeCache;

    public CacheManager(HazelcastNode hazelcast) {
        this.hazelcast = hazelcast.getHazelcastInstance();
        datamartCache = this.hazelcast.getMap("DatamartCache");
        datalakeCache = this.hazelcast.getMap("DatalakeCache");
    }

    @Override
    public Map<String, String[]> getInvertedIndex(String query) {
        String[] words = query.split("\\s+");

        Map<String, String[]> resultMap = new HashMap<>();
        for (String word : words) {
            Object content = datamartCache.get(word);
            if (content == null) {
                System.out.println("No inverted index found for word: " + word);
                continue;
            }
            String serializedAttributes = content.toString();
            String[] attributes = serializedAttributes.split(";");
            List<String> docIds = parseStringList(attributes[0]);
            List<List<Integer>> positions = parseNestedIntegerList(attributes[1]);
            List<Integer> frequencies = parseIntegerList(attributes[2]);
            String[] enrichedAttributes = new String[]{
                    docIds.toString(),
                    positions.toString(),
                    frequencies.toString()
            };
            try {
                resultMap.put(word, enrichedAttributes);
            } catch (Exception e) {
                System.out.println("Error deserializing attributes for word: " + word + " - " + e.getMessage());
            }
        }
        return resultMap;
    }

    @Override
    public Map<String, Document> getDocument(String documentId) {
        Object content = datalakeCache.get(documentId);
        if (content != null) {
            String documentContent = content.toString();
            if (!documentContent.isEmpty()) {
                Document document = new Document(documentId, documentContent);
                return Map.of(documentId, document);
            }
        }
        System.out.println("No document found for ID: " + documentId);
        return Map.of();
    }


    private List<String> parseStringList(String data) {
        data = data.replace("[", "").replace("]", "");
        if (data.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(data.split(",\\s*"));
    }

    private List<Integer> parseIntegerList(String data) {
        data = data.replace("[", "").replace("]", "");
        if (data.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String[] items = data.split(",\\s*");
        List<Integer> result = new ArrayList<>();
        for (String item : items) {
            result.add(Integer.parseInt(item.trim()));
        }
        return result;
    }

    private List<List<Integer>> parseNestedIntegerList(String data) {
        data = data.replace(" ", "").replace("],[", "]~[");
        data = data.substring(1, data.length() - 1);
        String[] nestedLists = data.split("~");

        List<List<Integer>> result = new ArrayList<>();
        for (String nested : nestedLists) {
            result.add(parseIntegerList(nested));
        }
        return result;
    }
}
