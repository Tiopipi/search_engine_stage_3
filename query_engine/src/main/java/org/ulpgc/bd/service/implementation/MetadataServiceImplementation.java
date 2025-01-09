package org.ulpgc.bd.service.implementation;

import org.example.model.Metadata;
import org.ulpgc.bd.service.interfaces.MetadataService;
import org.ulpgc.bd.utils.metadata.MetadataUtils;

import java.time.LocalDate;
import java.util.*;
import java.time.format.DateTimeParseException;


public class MetadataServiceImplementation implements MetadataService {

    private final List<Metadata> metadataList;


    public MetadataServiceImplementation(List<Metadata> metadataList) {
        this.metadataList = metadataList;
    }

    @Override
    public List<Metadata> searchMetadata(Map<String, String> filters) {
        List<Metadata> results = new ArrayList<>();
        for (Metadata metadata : metadataList) {
            if (matchesAllFilters(metadata, filters)) {
                results.add(metadata);
            }
        }
        return results;
    }

    private boolean matchesAllFilters(Metadata metadata, Map<String, String> filters) {
        return filters.entrySet().stream().allMatch(filter -> {
            String key = filter.getKey();
            String filterValue = filter.getValue();

            if (key.equals("from") || key.equals("to")) {
                return matchesDateRange(metadata, filters);
            }

            return filterValue == null || filterValue.trim().isEmpty() ||
                    MetadataUtils.getMetadataValue(metadata, key).contains(filterValue.toLowerCase());
        });
    }

    private boolean matchesDateRange(Metadata metadata, Map<String, String> filters) {
        LocalDate releaseDate = LocalDate.parse(metadata.getReleaseDate());

        try {
            LocalDate fromDate = filters.get("from").isEmpty() ? LocalDate.MIN : LocalDate.parse(filters.get("from"));
            LocalDate toDate = filters.get("to").isEmpty() ? LocalDate.MAX : LocalDate.parse(filters.get("to"));

            return (releaseDate.isEqual(fromDate) || releaseDate.isAfter(fromDate)) &&
                    (releaseDate.isEqual(toDate) || releaseDate.isBefore(toDate));
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format in filters: " + e.getMessage());
            return false;
        }
    }

}
