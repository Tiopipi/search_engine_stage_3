package org.ulpgc.bd.control;

import org.example.model.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Stream;

public interface BookLoader {
    Stream<Path> getBookPaths(String directory, Set<String> processedBooks);

    Document loadBook(Path bookPath);

    void initializeStopWords(String stopWordsFilePath) throws IOException;

    Set<String> getStopWords();

    String extractContent(String content, String startPattern);
}
