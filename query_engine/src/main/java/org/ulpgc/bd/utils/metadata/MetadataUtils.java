package org.ulpgc.bd.utils.metadata;

import org.example.model.Metadata;

public class MetadataUtils {

    private MetadataUtils() {
    }

    public static String getMetadataValue(Metadata metadata, String key) {
        return switch (key) {
            case "title" -> toLower(metadata.getTitle());
            case "author" -> toLower(metadata.getAuthor());
            case "language" -> toLower(metadata.getLanguage());
            case "year" -> extractYear(metadata.getReleaseDate());
            case "month" -> extractMonth(metadata.getReleaseDate());
            case "day" -> extractDay(metadata.getReleaseDate());
            default -> "";
        };
    }

    public static String toLower(String value) {
        return value != null ? value.toLowerCase() : "";
    }

    public static String extractYear(String releaseDate) {
        return extractDatePart(releaseDate, 0);
    }

    public static String extractMonth(String releaseDate) {
        return extractDatePart(releaseDate, 1);
    }

    public static String extractDay(String releaseDate) {
        return extractDatePart(releaseDate, 2);
    }

    private static String extractDatePart(String releaseDate, int part) {
        if (releaseDate != null && releaseDate.split("-").length > part) {
            return releaseDate.split("-")[part];
        }
        return "";
    }
}
