package org.ulpgc.bd.utils.stats;

import com.hazelcast.map.IMap;
import org.example.model.*;
import org.ulpgc.bd.repository.interfaces.*;
import org.ulpgc.bd.utils.hazelcast.HazelcastNode;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.*;

public class StatUtils {
    private final List<Metadata> metadataList;
    private final DatalakeLoaderInterface datalakeLoader;
    private final InvertedIndexLoader invertedIndexLoader;


    public StatUtils(InvertedIndexLoader invertedIndexLoader,
                     DatalakeLoaderInterface datalakeLoader,List<Metadata> metadataList) {
        this.invertedIndexLoader = invertedIndexLoader;
        this.datalakeLoader = datalakeLoader;
        this.metadataList = metadataList;
    }

    public int countAuthors() {
        HashSet<String> authors = new HashSet<>();

        for (Metadata metadata : metadataList) {
            String author = metadata.getAuthor();
            if (author != null && !author.trim().isEmpty()) {
                authors.add(author.trim());
            }
        }
        return authors.size();
    }


    public String showLanguages() {
        if (metadataList == null) {
            throw new IllegalStateException("Metadata list is not initialized.");
        }

        Map<String, Long> languageCounts = metadataList.stream()
                .map(Metadata::getLanguage)
                .filter(language -> language != null && !language.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.groupingBy(language -> language, Collectors.counting()));

        StringBuilder result = new StringBuilder();
        result.append("Languages: ").append(languageCounts.size()).append("\n");
        result.append("Total books per language: ").append("\n");

        languageCounts.forEach((language, count) ->
                result.append("- ").append(language).append(": ").append(count).append("\n")
        );

        return result.toString();
    }


    public Map<Integer, Long> countBooksPerYear() {
        return metadataList.stream()
                .map(Metadata::getReleaseDate)
                .map(date -> LocalDate.parse(date).getYear())
                .collect(Collectors.groupingBy(year -> year, Collectors.counting()));
    }


    public String topAuthor() {
        return metadataList.stream()
                .filter(metadata -> metadata.getAuthor() != null)
                .collect(Collectors.groupingBy(Metadata::getAuthor, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey() + " (" + entry.getValue() + " books)")
                .orElse("No authors available");
    }



    public String oldestBook() {
        return metadataList.stream()
                .min(Comparator.comparing(metadata -> LocalDate.parse(metadata.getReleaseDate())))
                .map(metadata -> "Title: " + metadata.getTitle() + ", Release Date: " + metadata.getReleaseDate())
                .orElse("No metadata available");
    }

    public String newestBook() {
        return metadataList.stream()
                .max(Comparator.comparing(metadata -> LocalDate.parse(metadata.getReleaseDate())))
                .map(metadata -> "Title: " + metadata.getTitle() + ", Release Date: " + metadata.getReleaseDate())
                .orElse("No metadata available");
    }

    public int countDocuments() {
        try (Stream<Path> files = datalakeLoader.loadBooksPath()) {
            return (int) files.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public int countWords() {
        int wordCount = 0;
        try {
            HazelcastNode hazelcastNode = HazelcastNode.getInstance();
            IMap<String, String> datamartMultiMap = hazelcastNode.getDatamartMap();

            if (datamartMultiMap != null) {
                wordCount = datamartMultiMap.keySet().size();
            }
        } catch (Exception e) {
            System.err.println("Error counting words: " + e.getMessage());
        }
        return wordCount;
    }

    public String documentWithHighestFrequency(String word) {
        InvertedIndex invertedIndex = invertedIndexLoader.loadInvertedIndex(word);

        if (invertedIndex == null) {
            return "Word not found in the index.";
        }

        return invertedIndex.getDocIds().stream()
                .max(Comparator.comparing(docId -> invertedIndex.getFrequencies().get(invertedIndex.getDocIds().indexOf(docId))))
                .map(docId -> {
                    int frequency = invertedIndex.getFrequencies().get(invertedIndex.getDocIds().indexOf(docId));

                    String title = metadataList.stream()
                            .filter(metadata -> metadata.getId().contains(docId))
                            .map(Metadata::getTitle)
                            .findFirst()
                            .orElse("Unknown Title");

                    return "Title: " + title + ", Frequency: " + frequency;
                })
                .orElse("No documents found for the word.");
    }

    private String mostSearchedWord(Map<String, Integer> searchFrequency) {
        int maxSearches = searchFrequency.values().stream()
                .max(Integer::compareTo)
                .orElse(0);

        List<String> mostSearchedWords = searchFrequency.entrySet().stream()
                .filter(entry -> entry.getValue() == maxSearches)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        return "Words: " + String.join(", ", mostSearchedWords) + ", Searches: " + maxSearches;
    }

    public String searchStatistics(Map<String, Integer> searchFrequency) {

        if (searchFrequency.isEmpty()) {
            return "No searches yet";
        }

        int totalSearches = searchFrequency.values().stream()
                .reduce(0, Integer::sum);

        String mostSearched = mostSearchedWord(searchFrequency);

        return "Total Searches: " + totalSearches + "\n" +
                "Most Searched: " + mostSearched + "\n" +
                "Unique Queries: " + searchFrequency.size();
    }

}
