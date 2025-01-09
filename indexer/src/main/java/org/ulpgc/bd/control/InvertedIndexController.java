package org.ulpgc.bd.control;

import org.example.model.Document;
import org.example.model.InvertedIndex;
import org.example.model.Metadata;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;


public class InvertedIndexController {

    private static final Logger logger = Logger.getLogger(InvertedIndexController.class.getName());
    private final BookLoader bookLoader;
    private final MetadataLoader metadataLoader;
    private final InvertedIndexManager invertedIndexManager;
    private final MetadataExporter metadataExporter;

    public InvertedIndexController(BookLoader bookLoader, MetadataLoader metadataLoader, InvertedIndexManager invertedIndexManager, MetadataExporter metadataExporter) {
        this.bookLoader = bookLoader;
        this.metadataLoader = metadataLoader;
        this.invertedIndexManager = invertedIndexManager;
        this.metadataExporter = metadataExporter;
    }

    public void processInvertedIndex(String booksDirectory, String stopWordsPath, String indexDirectory, Set<String> processedBooks) {
        try {
            bookLoader.initializeStopWords(stopWordsPath);

            try (Stream<Path> bookPaths = bookLoader.getBookPaths(booksDirectory, processedBooks)) {
                final int BATCH_SIZE = 100;
                List<Document> batch = new ArrayList<>();
                List<String> processedBatch = new ArrayList<>();

                bookPaths.forEach(bookPath -> {
                    try {
                        String bookId = extractBookId(bookPath);
                        if (!processedBooks.contains(bookId)) {
                            Document document = bookLoader.loadBook(bookPath);
                            if (document != null) {
                                batch.add(document);
                                processedBatch.add(bookId);
                            }
                        }
                        if (batch.size() >= BATCH_SIZE) {
                            processBatch(batch, processedBooks, indexDirectory);
                            processedBooks.addAll(processedBatch);
                            batch.clear();
                            processedBatch.clear();
                        }
                    } catch (Exception e) {
                        logger.warning("Error loading book: " + bookPath + " - " + e.getMessage());
                    }
                });

                if (!batch.isEmpty()) {
                    processBatch(batch, processedBooks, indexDirectory);
                    processedBooks.addAll(processedBatch);
                }
            }
        } catch (Exception e) {
            logger.severe("Error during InvertedIndex processing: " + e.getMessage());
        }
    }


    public void processMetadata(String booksDirectory, String metadataDirectory, Set<String> processedBooks) {
        try {
            List<Metadata> metadata = metadataLoader.loadMetadata(booksDirectory, processedBooks);
            metadataExporter.exportMetadata(metadata, metadataDirectory);
        } catch (Exception e) {
            logger.severe("Error during metadata processing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processBatch(List<Document> batch, Set<String> processedBooks, String indexDirectory) {
        try {
            if (processedBooks.isEmpty()) {
                Map<String, InvertedIndex> invertedIndexMap = invertedIndexManager.buildInvertedIndexWithPositions(
                        batch, bookLoader.getStopWords(), processedBooks, indexDirectory);
                invertedIndexManager.export(invertedIndexMap, indexDirectory);
            } else {
                Map<String, InvertedIndex> newEntries = invertedIndexManager.buildInvertedIndexWithPositions(
                        batch, bookLoader.getStopWords(), processedBooks, indexDirectory);
                invertedIndexManager.updateInvertedIndex(newEntries, indexDirectory);
            }
            logger.info("Processed batch of " + batch.size() + " documents.");
        } catch (Exception e) {
            logger.severe("Error processing batch: " + e.getMessage());
        }
    }

    private String extractBookId(Path bookPath) {
        return bookPath.getFileName().toString().replaceFirst("\\.txt$", "");
    }

}
