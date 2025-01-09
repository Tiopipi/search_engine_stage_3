package org.ulpgc.bd.repository.implementation;

import org.example.model.InvertedIndex;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.example.view.InvertedIndexView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class TreeDataStructureInvertedIndexLoader implements InvertedIndexLoader {

    private final String basePath;
    private final InvertedIndexView view;

    public TreeDataStructureInvertedIndexLoader(String basePath, InvertedIndexView view) {
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

        return invertedIndex;
    }

    private void loadWordIndex(String word,  InvertedIndex invertedIndex) throws IOException {
        Path filePath = Path.of(basePath, word.substring(0, 1), word.charAt(0) + "_words");
        Map<String, InvertedIndex> loadedData = view.read(filePath.toString());

        if (loadedData.containsKey(word)) {
            invertedIndex.mergeWith(loadedData.get(word));
        }
    }
}
