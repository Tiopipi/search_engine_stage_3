package org.ulpgc.bd.repository.implementation;

import org.example.model.Document;
import org.ulpgc.bd.repository.interfaces.DocumentLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TextDocumentLoader implements DocumentLoader {

    private final String booksPath;

    public TextDocumentLoader(String booksPath) {
        this.booksPath = booksPath;
    }

    @Override
    public Document loadDocument(String documentId) {
        try {
            Path filePath = Path.of(booksPath, documentId + ".txt");
            String content = Files.readString(filePath);
            return new Document(documentId,content);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Stream<Path> loadBooksPath() throws IOException {
        return Files.list(Path.of(booksPath));
    }

}
