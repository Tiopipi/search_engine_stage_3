package org.ulpgc.bd.implementation;

import org.ulpgc.bd.control.MetadataLoader;
import org.example.model.Metadata;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookMetadataLoader implements MetadataLoader {
    private static final Logger logger = Logger.getLogger(BookMetadataLoader.class.getName());
    private static final Map<String, String> METADATA_PATTERNS = Map.of(
            "title", "(Title|Título|Titre|Titel|Titolo|Título)\\s*:\\s*(.+)",
            "author", "(Author|Autor|Auteur|Verfasser|Autore|Contributor)\\s*:\\s*(.+)",
            "release_date", "(Release date|Fecha de publicación|Date de publication|Veröffentlichungsdatum|Data di pubblicazione|Data de publicação)\\s*:\\s*(.+)",
            "language", "(Language|Idioma|Langue|Sprache|Lingua|Língua)\\s*:\\s*(.+)"
    );

    @Override
    public List<Metadata> loadMetadata(String directory, Set<String> processedBooks) {
        List<Metadata> bookMetadata = new ArrayList<>();
        File folder = new File(directory);
        if (!folder.exists() || !folder.isDirectory()) {
            logger.warning("Provided directory does not exist or is not valid: " + folder.getPath());
            return bookMetadata;
        }
        for (File file : folder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".txt") && !processedBooks.contains(file.getName())) {
                try {
                    String content = Files.readString(file.toPath());
                    Metadata metadata = extractMetadata(content, file.getName());
                    bookMetadata.add(metadata);
                } catch (IOException e) {
                    logger.severe("Error reading file: " + file.getName() + " - " + e.getMessage());
                }
            }
        }
        return bookMetadata;
    }


    @Override
    public Metadata extractMetadata(String text, String documentId) {
        text = text.replaceAll("\\[.*?]", ""); // Limpieza del texto
        String title = extractField(text, METADATA_PATTERNS.get("title"));
        String author = extractField(text, METADATA_PATTERNS.get("author"));
        String releaseDate = extractField(text, METADATA_PATTERNS.get("release_date"));
        String language = extractField(text, METADATA_PATTERNS.get("language"));
        return new Metadata(title, author, releaseDate, language, documentId);
    }

    private String extractField(String text, String regex) {
        Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(2).trim() : "";
    }
}
