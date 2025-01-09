package org.ulpgc.bd.utils.singleProcessor;

import org.example.model.InvertedIndex;
import org.example.utils.CsvHandler;
import org.example.utils.Serializer;
import org.ulpgc.bd.utils.metadata.DateUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RecordProcessor implements RecordProcessorInterface{

    private static final Logger LOGGER = Logger.getLogger(RecordProcessor.class.getName());
    private final String datalakePath;
    private final String datamartPath;
    private final Serializer serializer;


    public RecordProcessor(String datalakePath, String datamartPath, Serializer serializer) {
        this.datalakePath = datalakePath;
        this.datamartPath = datamartPath;
        this.serializer = serializer;
    }


    public void processSingleMetadata(String documentId, String metadataPath, Map<String, List<String>> localMetadataMap) throws IOException {
        File metadataFile = new File(metadataPath);
        if (!isValidFile(metadataFile)) {
            LOGGER.warning("Metadata path is not valid: " + metadataPath);
            return;
        }

        List<String[]> rows = CsvHandler.readCsv(metadataPath, ";");
        String[] headers = rows.remove(0);

        for (String[] values : rows) {
            String id = getIdFromRow(headers, values);
            if (id.equals(documentId)) {
                List<String> attributes = getAttributesFromRow(headers, values);
                localMetadataMap.put(id, attributes);
                return;
            }
        }

        LOGGER.warning("No metadata found for document ID: " + documentId);
    }

    public void processSingleBook(String bookId, Map<String, String> datalakeMap) throws IOException {
        Path bookPath = Paths.get(datalakePath, bookId);

        if (!Files.exists(bookPath) || !Files.isRegularFile(bookPath)) {
            LOGGER.warning("Book file not found for ID: " + bookId + " at path: " + bookPath);
            return;
        }

        String content = Files.readString(bookPath);
        String documentId = bookId.replaceFirst("[.][^.]+$", "");

        datalakeMap.put(documentId, content);
    }

    public void processSingleWord(String word, ConcurrentHashMap<String, String> datamartMap) {
        Map<String, InvertedIndex> fileData = loadFromHierarchicalStructure(word);
        InvertedIndex invertedIndex = fileData.get(word);

        if (invertedIndex != null) {
            String serializedAttributes = serializeAttributes(invertedIndex);
            datamartMap.put(word, serializedAttributes);
        } else {
            LOGGER.warning("No data found for word: " + word);
        }
    }

    private Map<String, InvertedIndex> loadFromHierarchicalStructure(String word) {
        Path wordPath = resolveHierarchicalPath(word);

        if (!wordPath.toFile().exists() || !wordPath.toFile().isFile()) {
            LOGGER.warning("No file found for word: " + word);
            return Collections.emptyMap();
        }

        Map<String, InvertedIndex> fileData = new HashMap<>();

        try {
            Map<String, InvertedIndex> data = serializer.read(wordPath.toString());
            fileData.putAll(data);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error reading file: " + wordPath.getFileName(), e);
            throw new RuntimeException("Failed to read file for word: " + word, e);
        }

        return fileData;
    }

    private Path resolveHierarchicalPath(String key) {
        Path path = Path.of(datamartPath);
        String level1 = key.substring(0, 1);
        String level2 = key.length() >= 2 ? key.substring(0, 2) : level1;
        String level3 = key.length() >= 3 ? key.substring(0, 3) : level2;

        return path.resolve(level1).resolve(level2).resolve(level3).resolve(key);
    }

    private String serializeAttributes(InvertedIndex invertedIndex) {
        return String.join(";",
                invertedIndex.getDocIds().toString(),
                invertedIndex.getPositions().toString(),
                invertedIndex.getFrequencies().toString()
        );
    }

    private boolean isValidFile(File file) {
        return file.exists();
    }

    private String getIdFromRow(String[] headers, String[] values) {
        return values[Arrays.asList(headers).indexOf("document")];
    }

    private List<String> getAttributesFromRow(String[] headers, String[] values) {
        return Arrays.asList(
                values[Arrays.asList(headers).indexOf("title")],
                values[Arrays.asList(headers).indexOf("author")],
                DateUtils.convertDate(
                        values[Arrays.asList(headers).indexOf("release_date")],
                        "MMMM d, yyyy", "yyyy-MM-dd"
                ),
                values[Arrays.asList(headers).indexOf("language")]
        );
    }
}
