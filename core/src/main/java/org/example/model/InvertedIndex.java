package org.example.model;

import java.util.ArrayList;
import java.util.List;

public class InvertedIndex {

    private List<String> id = new ArrayList<>();
    private List<List<Integer>> positions = new ArrayList<>();
    private List<Integer> frequencies = new ArrayList<>();

    public List<String> getDocIds() {
        return id;
    }

    public void setDocIds(List<String> docIds) {
        this.id = docIds;
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

    public void addPosition(String docId, int position) {
        int index = id.indexOf(docId);
        if (index == -1) {
            id.add(docId);
            positions.add(new ArrayList<>(List.of(position)));
            frequencies.add(1);
        } else {
            positions.get(index).add(position);
            frequencies.set(index, frequencies.get(index) + 1);
        }
    }

    public void mergeWith(InvertedIndex newEntry) {
        for (int i = 0; i < newEntry.getDocIds().size(); i++) {
            String docId = newEntry.getDocIds().get(i);
            int index = id.indexOf(docId);

            if (index != -1) {
                positions.get(index).addAll(newEntry.getPositions().get(i));
                frequencies.set(index, frequencies.get(index) + newEntry.getFrequencies().get(i));
            } else {
                id.add(docId);
                positions.add(newEntry.getPositions().get(i));
                frequencies.add(newEntry.getFrequencies().get(i));
            }
        }
    }
}
