package org.ulpgc.bd.repository.implementation;

import org.example.model.InvertedIndex;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.example.view.InvertedIndexView;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class HierarchicalInvertedIndexLoader implements InvertedIndexLoader {

    private final String basePath;
    private final InvertedIndexView view;

    public HierarchicalInvertedIndexLoader(String basePath, InvertedIndexView view) {
        this.basePath = basePath;
        this.view = view;
    }

    @Override
    public InvertedIndex loadInvertedIndex(String query) {
        InvertedIndex invertedIndex = new InvertedIndex();
        for (String word : query.split("\\s+")) {
            try {
                loadWordIndex(word, invertedIndex);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println(invertedIndex);

        return invertedIndex;
    }


    private void loadWordIndex(String word, InvertedIndex invertedIndex) throws IOException {
        Path filePath = resolveHierarchicalPath(word);
        if (!Files.exists(filePath)) {
            return;
        }
        Map<String, InvertedIndex> loadedData = view.read(filePath.toString());
        if (loadedData == null) {
            return;
        }
        if (loadedData.containsKey(word)) {
            invertedIndex.mergeWith(loadedData.get(word));
        } 
    }

    private Path resolveHierarchicalPath(String key) {
        Path path = Path.of(basePath);
        String level1 = key.substring(0, 1);
        String level2 = key.length() >= 2 ? key.substring(0, 2) : level1;
        String level3 = key.length() >= 3 ? key.substring(0, 3) : level2;

        return path.resolve(level1).resolve(level2).resolve(level3).resolve(key);
    }

    public String getBasepath(){
        return basePath;
    }
}
