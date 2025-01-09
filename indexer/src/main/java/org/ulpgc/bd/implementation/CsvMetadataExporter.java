package org.ulpgc.bd.implementation;

import org.ulpgc.bd.control.MetadataExporter;
import org.example.model.Metadata;
import org.example.utils.CsvHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvMetadataExporter implements MetadataExporter {

    @Override
    public void exportMetadata(List<Metadata> metadataList, String metadataDirectory) {
        Path parentDir = createDirectory(metadataDirectory);
        if (parentDir == null) return;

        Path outputPath = parentDir.resolve("metadata.csv");
        try {
            List<String> rows = prepareCsvData(metadataList, outputPath);
            CsvHandler.writeCsv(rows, outputPath);
        } catch (IOException e) {
            System.err.println("Error writing to CSV: " + e.getMessage());
        }
    }

    private Path createDirectory(String metadataDirectory) {
        Path parentDir = Paths.get(metadataDirectory);
        if (!Files.exists(parentDir)) {
            try {
                Files.createDirectories(parentDir);
            } catch (IOException e) {
                System.err.println("Error creating directories: " + e.getMessage());
                return null;
            }
        }
        return parentDir;
    }

    private List<String> prepareCsvData(List<Metadata> metadataList, Path outputPath) throws IOException {
        List<String> rows = new ArrayList<>();
        if (isFileEmpty(outputPath, metadataList)) {
            rows.add("release_date;author;document;language;title");
        }
        for (Metadata metadata : metadataList) {
            rows.add(String.join(";",
                    metadata.getReleaseDate(),
                    metadata.getAuthor(),
                    metadata.getId(),
                    metadata.getLanguage(),
                    metadata.getTitle()));
        }
        return rows;
    }

    private boolean isFileEmpty(Path outputPath, List<Metadata> metadataList) throws IOException {
        return !Files.exists(outputPath) || Files.size(outputPath) == 0 && !metadataList.isEmpty();
    }
}
