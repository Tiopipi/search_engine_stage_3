package org.ulpgc.bd.implementation;

import org.ulpgc.bd.control.BookLoader;
import org.example.model.Document;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.*;
import java.util.stream.Stream;

public class BookFileLoader implements BookLoader {
    private static final Logger logger = Logger.getLogger(BookFileLoader.class.getName());
    private static final Set<String> stopWords = new HashSet<>();

    @Override
    public Stream<Path> getBookPaths(String directory, Set<String> processedBooks) {
        try {
            return Files.walk(Paths.get(directory))
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .filter(path -> !processedBooks.contains(path.getFileName().toString()));
        } catch (IOException e) {
            logger.warning("Error loading book paths: " + e.getMessage());
            return Stream.empty();
        }
    }

    @Override
    public Document loadBook(Path bookPath) {
        try {
            String bookFileName = bookPath.getFileName().toString();
            String content = Files.readString(bookPath);
            String bookContent = extractContent(content, ".* START OF .*");
            if (bookContent != null) {
                return new Document(bookFileName, bookContent);
            }
        } catch (IOException e) {
            logger.warning("Error processing book: " + bookPath.getFileName() + " - " + e.getMessage());
        }
        return null;
    }

    @Override
    public String extractContent(String content, String startPattern) {
        Pattern pattern = Pattern.compile(startPattern, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);
        return matcher.find() ? content.substring(matcher.end()).trim() : null;
    }


    @Override
    public void initializeStopWords(String stopWordsFilePath) throws IOException {
        String content = new String(Files.readAllBytes(Paths.get(stopWordsFilePath)), StandardCharsets.UTF_8);
        parseStopWords(content);
    }

    private void parseStopWords(String content) {
        content = content.replaceAll("[{}\\s]", "");
        String[] words = content.split(",");
        for (String word : words) {
            stopWords.add(word.replace("'", "").toLowerCase().trim());
        }
    }


    @Override
    public Set<String> getStopWords() {
        return stopWords;
    }
}
