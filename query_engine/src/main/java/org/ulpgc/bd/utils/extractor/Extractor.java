package org.ulpgc.bd.utils.extractor;

import org.example.model.InvertedIndex;
import org.example.view.InvertedIndexView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Extractor implements ExtractorInterface{

    private final String basePath;
    private final InvertedIndexView view;

    public Extractor(String basePath, InvertedIndexView view) {
        this.basePath = basePath;
        this.view = view;
    }

    @Override
    public List<String> extractAllWords() throws IOException {
        List<String> allWords = new ArrayList<>();

        Files.walk(Path.of(basePath))
                .filter(Files::isRegularFile)
                .forEach(filePath -> {
                    try {
                        Map<String, InvertedIndex> loadedData = view.read(filePath.toString());
                        if (loadedData != null) {
                            allWords.addAll(loadedData.keySet());
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return allWords;
    }

    @Override
    public  List<String> extractBookIdsFromCsv(String metadataFilePath) {
        List<String> bookIds = new ArrayList<>();
        String line;
        try (BufferedReader br = new BufferedReader(new FileReader(metadataFilePath))) {
            br.readLine();

            while ((line = br.readLine()) != null) {
                String[] columns = line.split(";");
                String bookId = columns[2];
                bookIds.add(bookId);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookIds;
    }
}
