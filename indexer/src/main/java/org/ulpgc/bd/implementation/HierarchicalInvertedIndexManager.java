package org.ulpgc.bd.implementation;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.ulpgc.bd.utils.MapUtils;
import org.ulpgc.bd.utils.TextUtils;
import org.example.view.InvertedIndexView;
import org.example.model.InvertedIndex;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class HierarchicalInvertedIndexManager extends AbstractInvertedIndexManager {
    private static final Logger logger = Logger.getLogger(HierarchicalInvertedIndexManager.class.getName());
    private final InvertedIndexView view;

    public HierarchicalInvertedIndexManager(InvertedIndexView view) {
        super(new StandardAnalyzer());
        this.view = view;
    }

    @Override
    public void export(Map<String, InvertedIndex> invertedIndex, String baseDirectory) {
        invertedIndex.forEach((word, entry) -> {
            String hierarchyPath = buildHierarchyPath(word);
            Path directoryPath = Paths.get(baseDirectory, hierarchyPath);
            Path filePath = directoryPath.resolve(word);
            try {
                Files.createDirectories(directoryPath);
                view.write(Map.of(word, entry), filePath.toString());
            } catch (IOException e) {
                logger.warning("Error exporting word " + word + ": " + e.getMessage());
            }
        });
    }

    @Override
    public void updateInvertedIndex(Map<String, InvertedIndex> newEntries, String baseDirectory) {
        newEntries.forEach((word, newData) -> {
            List<String> normalizedWords = TextUtils.cleanAndNormalize(word, analyzer);

            for (String normalizedWord : normalizedWords) {
                String hierarchyPath = buildHierarchyPath(normalizedWord);
                Path directoryPath = Paths.get(baseDirectory, hierarchyPath);
                Path filePath = directoryPath.resolve(normalizedWord);

                try {
                    if (!Files.exists(directoryPath)) {
                        Files.createDirectories(directoryPath);
                    }

                    Map<String, InvertedIndex> existingData;
                    if (Files.exists(filePath)) {
                        Map<String, InvertedIndex> readData = view.read(filePath.toString());
                        existingData = new HashMap<>(readData);
                    } else {
                        existingData = new HashMap<>();
                    }

                    Map<String, InvertedIndex> newDataMapped = Map.of(normalizedWord, newData);
                    MapUtils.mergeInvertedIndexData(existingData, newDataMapped);
                    view.write(existingData, filePath.toString());
                } catch (IOException e) {
                    logger.warning("Error updating word " + normalizedWord + ": " + e.getMessage());
                }
            }
        });
    }

    private String buildHierarchyPath(String word) {
        String cleanedWord = word.toLowerCase();
        if (cleanedWord.isEmpty()) {
            return "unknown";
        }
        String level1 = cleanedWord.substring(0, 1).toLowerCase();
        String level2 = cleanedWord.length() > 1 ? cleanedWord.substring(0, 2).toLowerCase() : level1;
        String level3 = cleanedWord.length() > 2 ? cleanedWord.substring(0, 3).toLowerCase() : level2;
        return String.join(File.separator, level1, level2, level3);
    }
}
