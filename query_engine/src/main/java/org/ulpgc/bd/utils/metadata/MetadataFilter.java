package org.ulpgc.bd.utils.metadata;

import org.example.model.Metadata;
import org.ulpgc.bd.service.interfaces.MetadataService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class MetadataFilter implements MetadataFilterInterface {

    private final MetadataService metadataService;

    public MetadataFilter(MetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @Override
    public List<Metadata> getFilteredAndDateRangeMetadata(Map<String, String> params) {
        Map<String, String> filters = buildFilters(params);
        List<Metadata> filteredMetadata = metadataService.searchMetadata(filters);

        if (hasDateRange(params)) {
            LocalDate fromDate = parseDate(params.get("from"));
            LocalDate toDate = parseDate(params.get("to"));
            filteredMetadata = filterByDateRange(filteredMetadata, fromDate, toDate);
        }
        return filteredMetadata;
    }

    private Map<String, String> buildFilters(Map<String, String> params) {
        return Map.of(
                "title", params.get("title"),
                "author", params.get("author"),
                "year", params.get("year"),
                "month", params.get("month"),
                "day", params.get("day"),
                "language", params.get("language"),
                "from", params.get("from"),
                "to", params.get("to")
        );
    }


    private boolean hasDateRange(Map<String, String> params) {
        return params.containsKey("from") || params.containsKey("to");
    }


    private LocalDate parseDate(String dateStr) {
        try {
            if (!dateStr.isEmpty()) {
                return LocalDate.parse(dateStr);
            }
        } catch (DateTimeParseException e) {
            System.err.println("Invalid date format: " + dateStr);
        }
        return null;
    }


    private List<Metadata> filterByDateRange(List<Metadata> metadataList, LocalDate fromDate, LocalDate toDate) {
        return metadataList.stream()
                .filter(metadata -> {
                    LocalDate releaseDate = LocalDate.parse(metadata.getReleaseDate());
                    boolean isAfterFromDate = fromDate == null || !releaseDate.isBefore(fromDate);
                    boolean isBeforeToDate = toDate == null || !releaseDate.isAfter(toDate);
                    return isAfterFromDate && isBeforeToDate;
                })
                .collect(Collectors.toList());
    }
}
