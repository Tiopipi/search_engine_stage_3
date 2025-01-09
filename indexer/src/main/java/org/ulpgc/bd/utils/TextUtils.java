package org.ulpgc.bd.utils;

import org.apache.lucene.analysis.Analyzer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public class TextUtils {
    private static final Logger logger = Logger.getLogger(TextUtils.class.getName());

    private TextUtils() {
    }

    public static List<String> cleanAndNormalize(String input, Analyzer analyzer) {
        if (input == null || input.isEmpty()) {
            return Collections.emptyList();
        }

        input = input.toLowerCase();
        String[] words = input.split("[^a-z0-9áéíóúàèìòùäëïöüâêîôûçñ']+");

        List<String> validWords = new ArrayList<>();
        for (String word : words) {
            if (!word.isEmpty() && word.matches("[a-z0-9áéíóúàèìòùäëïöüâêîôûçñ][a-z0-9áéíóúàèìòùäëïöüâêîôûçñ']*")) {
                validWords.add(word);
            }
        }

        return validWords;
    }

    public static String extractFirstLetter(String word) {
        return word.substring(0, 1).toLowerCase().replaceAll("[^a-z0-9áéíóúàèìòùäëïöüâêîôûçñ]", "");
    }
}
