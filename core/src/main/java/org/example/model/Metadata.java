package org.example.model;

import java.util.HashMap;
import java.util.Map;

public class Metadata {
    private final Map<String, String> metadata;

    public Metadata(String title, String author, String releaseDate, String language, String id) {
        metadata = new HashMap<>();
        metadata.put("title", title);
        metadata.put("author", author);
        metadata.put("releaseDate", releaseDate);
        metadata.put("language", language);
        metadata.put("id", id);
    }

    public String getTitle() {
        return metadata.get("title");
    }

    public String getAuthor() {
        return metadata.get("author");
    }

    public String getReleaseDate() {
        return metadata.get("releaseDate");
    }

    public String getLanguage() {
        return metadata.get("language");
    }

    public String getId() {
        return metadata.get("id");
    }
}
