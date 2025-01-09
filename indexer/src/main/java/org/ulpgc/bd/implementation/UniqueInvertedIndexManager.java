package org.ulpgc.bd.implementation;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.ulpgc.bd.utils.MapUtils;
import org.example.view.InvertedIndexView;
import org.example.model.InvertedIndex;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UniqueInvertedIndexManager extends AbstractInvertedIndexManager {
    private final InvertedIndexView view;

    public UniqueInvertedIndexManager(InvertedIndexView view) {
        super(new StandardAnalyzer());
        this.view = view;
    }

    @Override
    public void export(Map<String, InvertedIndex> invertedIndex, String baseDirectory) {
        String filePath = baseDirectory + "/invertedIndex";
        File directory = new File(baseDirectory);
        if (!directory.exists()) {directory.mkdirs();}
        try {
            view.write(invertedIndex, filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateInvertedIndex(Map<String, InvertedIndex> newEntries, String baseDirectory) {
        String filePath = baseDirectory + "/invertedIndex";
        try {
            Map<String, InvertedIndex> existingData = view.read(filePath);
            if (existingData == null) {
                existingData = new HashMap<>();
            }
            MapUtils.mergeInvertedIndexData(existingData, newEntries);
            export(existingData, baseDirectory);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
