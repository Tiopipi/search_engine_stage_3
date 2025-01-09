package org.ulpgc.bd.model;

import java.util.ArrayList;
import java.util.List;

public class QueryResult  {

    private List<String> id = new ArrayList<>();
    private List<List<Integer>> positions = new ArrayList<>();
    private List<Integer> frequencies = new ArrayList<>();
    private final List<String> paragraphs = new ArrayList<>();
    private final List<String> titles = new ArrayList<>();

    public List<String> getDocIds() {
        return id;
    }

    public void setDocIds(List<String> id) {
        this.id = id;
    }

    public List<List<Integer>> getPositions() {
        return positions;
    }

    public void setPositions(List<List<Integer>> positions) {
        this.positions = positions;
    }

    public List<Integer> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(List<Integer> frequencies) {
        this.frequencies = frequencies;
    }

    public List<String> getParagraphs() {
        return paragraphs;
    }


    public void setParagraphs(List<String> paragraphs) {
        this.paragraphs.clear();
        this.paragraphs.addAll(paragraphs);
    }

    public List<String> getTitles() {
        return titles;
    }

    public void setTitles(List<String> titles) {
        this.titles.clear();
        this.titles.addAll(titles);
    }
}
