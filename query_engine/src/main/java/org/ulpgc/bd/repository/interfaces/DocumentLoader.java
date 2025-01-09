package org.ulpgc.bd.repository.interfaces;

import org.example.model.Document;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface DocumentLoader {
    Document loadDocument(String documentId);
    Stream<Path> loadBooksPath() throws IOException;
}
