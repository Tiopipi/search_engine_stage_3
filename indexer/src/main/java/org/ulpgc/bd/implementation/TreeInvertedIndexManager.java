package org.ulpgc.bd.implementation;

import org.example.model.InvertedIndex;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.ulpgc.bd.utils.MapUtils;
import org.ulpgc.bd.utils.TextUtils;
import org.example.view.InvertedIndexView;

import java.io.IOException;
import java.nio.file.*;
import java.text.Normalizer;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TreeInvertedIndexManager extends AbstractInvertedIndexManager {
    private static final Logger logger = Logger.getLogger(TreeInvertedIndexManager.class.getName());
    private final InvertedIndexView view;

    public TreeInvertedIndexManager(InvertedIndexView view) {
        super(new StandardAnalyzer());
        this.view = view;
    }

    @Override
    public void export(Map<String, InvertedIndex> invertedIndex, String baseDirectory) {
        Map<String, Map<String, InvertedIndex>> groupedData = groupByFirstLetter(invertedIndex);
        groupedData.forEach((letter, data) -> writeGroupedData(letter, data, baseDirectory));
    }

    private Map<String, Map<String, InvertedIndex>> groupByFirstLetter(Map<String, InvertedIndex> invertedIndex) {
        Map<String, Map<String, InvertedIndex>> groupedData = new HashMap<>();
        for (Map.Entry<String, InvertedIndex> entry : invertedIndex.entrySet()) {
            String firstLetter = TextUtils.extractFirstLetter(entry.getKey());
            if (!firstLetter.isEmpty()) {
                groupedData.computeIfAbsent(firstLetter, k -> new HashMap<>()).put(entry.getKey(), entry.getValue());
            }
        }
        return groupedData;
    }

    private void writeGroupedData(String letter, Map<String, InvertedIndex> data, String baseDirectory) {
        String safeLetter = Normalizer.normalize(letter, Normalizer.Form.NFC);
        Path directoryPath = Paths.get(baseDirectory, safeLetter);
        Path filePath = directoryPath.resolve(safeLetter + "_words");
        try {
            Files.createDirectories(directoryPath);
            view.write(data, filePath.toString());
        } catch (IOException e) {
            logger.warning("Error exporting index for letter " + letter + ": " + e.getMessage());
        }
    }

    @Override
    public void updateInvertedIndex(Map<String, InvertedIndex> newEntries, String baseDirectory) {
        Map<String, Map<String, InvertedIndex>> groupedNewEntries = groupByFirstLetter(newEntries);
        groupedNewEntries.forEach((letter, newData) -> updateGroupedData(letter, newData, baseDirectory));
    }

    private void updateGroupedData(String letter, Map<String, InvertedIndex> newData, String baseDirectory) {
        String safeLetter = Normalizer.normalize(letter, Normalizer.Form.NFC);
        Path directoryPath = Paths.get(baseDirectory, safeLetter);
        Path filePath = directoryPath.resolve(safeLetter + "_words");

        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            Map<String, InvertedIndex> existingData = view.read(filePath.toString());
            if (existingData == null) {
                existingData = new HashMap<>();
            }

            MapUtils.mergeInvertedIndexData(existingData, newData);

            view.write(existingData, filePath.toString());
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error updating index for letter " + letter +
                    " in directory " + directoryPath + " with file path " + filePath, e);
        }
    }
}
