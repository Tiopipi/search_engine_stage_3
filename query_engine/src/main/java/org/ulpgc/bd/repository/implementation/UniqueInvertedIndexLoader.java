package org.ulpgc.bd.repository.implementation;

import org.example.model.InvertedIndex;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.example.view.InvertedIndexView;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

public class UniqueInvertedIndexLoader implements InvertedIndexLoader {

    private final String filePath;
    private final InvertedIndexView view;

    public UniqueInvertedIndexLoader(String filePath, InvertedIndexView view) {
        this.filePath = filePath;
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

    private void loadWordIndex(String word,InvertedIndex invertedIndex) throws IOException {
        Path path = Path.of(filePath);
        Map<String, InvertedIndex> loadedData = view.read(path.toString());

        if (loadedData.containsKey(word)) {
            invertedIndex.mergeWith(loadedData.get(word));
        }
    }
}
