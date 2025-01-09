package org.ulpgc.bd.utils.document;

import org.example.model.Document;
import java.util.regex.*;

public class DocumentUtils {

    private DocumentUtils() {
    }

    public static String extractParagraphByCharPosition(Document document, int wordPosition) {
        String content = document.getContent();

        String textAfterMarker = extractTextAfterMarker(content);
        if (textAfterMarker.isEmpty()) {
            return "";
        }

        int startWord = Math.max(0, wordPosition - 10);
        int endWord = wordPosition + 10;

        return extractWordsInRange(textAfterMarker, startWord, endWord);
    }

    private static String extractTextAfterMarker(String content) {
        String markerPattern = "\\*\\*\\* START OF THE PROJECT GUTENBERG EBOOK.*?\\*\\*\\*";
        Pattern pattern = Pattern.compile(markerPattern, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (!matcher.find()) {
            return "";
        }

        return content.substring(matcher.end()).trim();
    }

    private static String extractWordsInRange(String text, int startWord, int endWord) {
        StringBuilder result = new StringBuilder();
        int currentWord = 0;
        int wordStart = -1;

        for (int i = 0; i <= text.length(); i++) {
            char currentChar = i < text.length() ? text.charAt(i) : ' ';
            boolean isWordBoundary = Character.isWhitespace(currentChar);

            if (!isWordBoundary && wordStart == -1) {
                wordStart = i;
            } else if (isWordBoundary && wordStart != -1) {
                processWord(result, text, wordStart, i, currentWord, startWord, endWord);
                currentWord++;
                wordStart = -1;

                if (currentWord > endWord) {
                    break;
                }
            }
        }

        return result.toString().trim();
    }

    private static void processWord(StringBuilder result, String text, int wordStart, int wordEnd, int currentWord, int startWord, int endWord) {
        if (currentWord >= startWord && currentWord <= endWord) {
            result.append(text, wordStart, wordEnd).append(" ");
        }
    }


    public static String extractTitle(Document document) {
        Matcher matcher = Pattern.compile("(Title|Título|Titre|Titel|Titolo)\\s*:\\s*([^\\n]+)").matcher(document.getContent());
        return matcher.find() ? matcher.group(2).trim() : "";
    }
}
