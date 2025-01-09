package org.example.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.example.model.InvertedIndex;


public class JsonSerializer implements Serializer {
    private final Gson gson;

    public JsonSerializer() {
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    private static class InvertedIndexJson {
        @SerializedName("id")
        private final List<String> docIds;

        @SerializedName("p")
        private final List<List<Integer>> positions;

        @SerializedName("f")
        private final List<Integer> frequencies;

        public InvertedIndexJson(InvertedIndex base) {
            this.docIds = base.getDocIds();
            this.positions = base.getPositions();
            this.frequencies = base.getFrequencies();
        }

        public InvertedIndex toInvertedIndex() {
            InvertedIndex base = new InvertedIndex();
            base.setDocIds(this.docIds);
            base.setPositions(this.positions);
            base.setFrequencies(this.frequencies);
            return base;
        }
    }

    @Override
    public void write(Map<String, InvertedIndex> data, String filePath) throws IOException {
        Map<String, InvertedIndexJson> jsonData = new HashMap<>();
        data.forEach((key, value) -> jsonData.put(key, new InvertedIndexJson(value)));

        try (FileWriter fileWriter = new FileWriter(filePath)) {
            gson.toJson(jsonData, fileWriter);
        }
    }

    @Override
    public Map<String, InvertedIndex> read(String filePath) throws IOException {
        File jsonFile = new File(filePath);
        if (!jsonFile.exists()) {
            return null;
        }

        try (FileReader reader = new FileReader(jsonFile)) {
            Type type = new TypeToken<Map<String, InvertedIndexJson>>() {}.getType();
            Map<String, InvertedIndexJson> jsonData = gson.fromJson(reader, type);

            Map<String, InvertedIndex> data = new HashMap<>();
            jsonData.forEach((key, value) -> data.put(key, value.toInvertedIndex()));
            return data;
        }
    }
}
