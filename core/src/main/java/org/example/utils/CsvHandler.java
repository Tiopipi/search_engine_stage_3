package org.example.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

public class CsvHandler {

    public static List<String[]> readCsv(String filePath, String delimiter) throws IOException {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                rows.add(line.split(delimiter));
            }
        }
        return rows;
    }

    public static void writeCsv(List<String> rows, Path outputPath) throws IOException {
        if (!Files.exists(outputPath)) {
            Files.createFile(outputPath);
        }
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardOpenOption.APPEND)) {
            for (String row : rows) {
                writer.write(row);
                writer.newLine();
            }
        }
    }
}
