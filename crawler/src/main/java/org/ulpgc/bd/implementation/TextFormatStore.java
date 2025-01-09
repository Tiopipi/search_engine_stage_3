package org.ulpgc.bd.implementation;

import org.ulpgc.bd.control.BookStore;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class TextFormatStore implements BookStore {

    @Override
    public void storeBook(String urlBook, String bookId, String REPOSITORY_DOCUMENTS, Logger logger) {
        try {
            Files.createDirectories(Path.of(REPOSITORY_DOCUMENTS));
            downloadFile(urlBook, REPOSITORY_DOCUMENTS + "/" + bookId + ".txt", logger);
        } catch (IOException e) {
            logger.warning("Error storing book: " + urlBook + " - " + e.getMessage());
        }
    }

    public static void downloadFile(String fileUrl, String outputPath, Logger logger) throws IOException {
        try (InputStream in = new URL(fileUrl).openStream();
             FileOutputStream out = new FileOutputStream(outputPath)) {
            in.transferTo(out);
            logger.info("File downloaded successfully to: " + outputPath);
        }
    }
}
