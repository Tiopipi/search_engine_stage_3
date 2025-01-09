package org.ulpgc.bd.utils.batch;

import org.ulpgc.bd.utils.singleProcessor.RecordProcessorInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.concurrent.ForkJoinPool;
import java.util.logging.Logger;

public class BatchProcessor implements BatchProcessorInterface{

    private static final Logger LOGGER = Logger.getLogger(BatchProcessor.class.getName());
    private final RecordProcessorInterface recordProcessor;

    public BatchProcessor(RecordProcessorInterface recordProcessor) {
        this.recordProcessor = recordProcessor;
    }

    public void processBatchMetadata(List<String> batch, String metadataPath, Map<String, List<String>> localMetadataMap) {
        ForkJoinPool.commonPool().submit(() -> {
            batch.parallelStream().forEach(documentId -> {
                try {
                    recordProcessor.processSingleMetadata(documentId, metadataPath, localMetadataMap);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error processing metadata for document: " + documentId, e);
                }
            });
        }).join();
    }

    public void processBatchDatalake(List<String> batch, Map<String, String> datalakeMap) {
        for (String bookId : batch) {
            try {
                recordProcessor.processSingleBook(bookId, datalakeMap);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error processing book: " + bookId, e);
            }
        }
    }

    public void processBatchConcurrently(List<String> batch, ConcurrentHashMap<String, String> datamartMap,
                                          ExecutorService executorService) {
        List<Callable<Void>> tasks = new ArrayList<>();

        for (String word : batch) {
            tasks.add(() -> {
                try {
                    recordProcessor.processSingleWord(word, datamartMap);
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error processing word: " + word, e);
                }
                return null;
            });
        }

        try {
            executorService.invokeAll(tasks);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Error while processing batch concurrently", e);
            Thread.currentThread().interrupt();
        }
    }

}
