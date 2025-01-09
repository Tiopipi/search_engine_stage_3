package org.example.utils;

import org.example.model.InvertedIndex;

import java.io.IOException;
import java.util.Map;

public interface Serializer {
    void write(Map<String, InvertedIndex> data, String filePath) throws IOException;

    Map<String, InvertedIndex> read(String filePath) throws IOException;
}
