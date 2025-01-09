package org.ulpgc.bd.control;

import org.example.model.Metadata;

import java.util.List;
import java.util.Set;

public interface MetadataLoader {
    List<Metadata> loadMetadata(String directory, Set<String> processedBooks);
    Metadata extractMetadata(String text, String documentId);
}
