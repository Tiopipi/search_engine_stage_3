package org.ulpgc.bd.repository.interfaces;

import org.example.model.InvertedIndex;

public interface InvertedIndexLoader {
    InvertedIndex loadInvertedIndex(String query);
}
