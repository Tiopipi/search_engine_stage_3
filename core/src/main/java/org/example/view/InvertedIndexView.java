package org.example.view;

import org.example.utils.Serializer;
import org.example.model.InvertedIndex;

import java.io.IOException;
import java.util.Map;

public class InvertedIndexView {
    private final Serializer serializer;

    public InvertedIndexView(Serializer serializer) {
        this.serializer = serializer;
    }

    public void write(Map<String, InvertedIndex> data, String filePath) throws IOException {
        serializer.write(data, filePath);
    }

    public Map<String, InvertedIndex> read(String filePath) throws IOException {
        return serializer.read(filePath);
    }
}
