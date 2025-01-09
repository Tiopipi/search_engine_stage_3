package org.ulpgc.bd.apps;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import org.ulpgc.bd.control.*;
import org.ulpgc.bd.implementation.*;
import org.example.utils.*;
import org.example.view.InvertedIndexView;

public class Main {

    public static void main(String[] args) {
        String BOOKS_DIRECTORY = System.getenv("BOOKS_DIRECTORY");
        String STOP_WORDS = System.getenv("STOP_WORDS");
        String INDEX_DIRECTORY_TREE = System.getenv("INDEX_DIRECTORY_TREE");
        String INDEX_DIRECTORY_UNIQUE = System.getenv("INDEX_DIRECTORY_UNIQUE");
        String INDEX_DIRECTORY_HIERARCHICAL = System.getenv("INDEX_DIRECTORY_HIERARCHICAL");
        String METADATA_DIRECTORY = System.getenv("METADATA_DIRECTORY");
        String PROCESSED_BOOKS_PATH = System.getenv("PROCESSED_BOOKS_PATH");

        BookLoader bookLoader = new BookFileLoader();
        Serializer serializer = new BinarySerializer();
        InvertedIndexView view = new InvertedIndexView(serializer);

        MetadataLoader metadataLoader = new BookMetadataLoader();
        InvertedIndexManager treeInvertedIndexManager = new TreeInvertedIndexManager(view);
        InvertedIndexManager uniqueIndexer = new UniqueInvertedIndexManager(view);
        InvertedIndexManager hierarchicalInvertedIndexManager = new HierarchicalInvertedIndexManager(view);
        MetadataExporter metadataExporter = new CsvMetadataExporter();

        InvertedIndexController controllerTree = new InvertedIndexController(bookLoader, metadataLoader, treeInvertedIndexManager, metadataExporter);
        InvertedIndexController controllerUnique = new InvertedIndexController(bookLoader, metadataLoader, uniqueIndexer, metadataExporter);
        InvertedIndexController controllerHierarchical = new InvertedIndexController(bookLoader, metadataLoader, hierarchicalInvertedIndexManager, metadataExporter);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduler.scheduleAtFixedRate(() -> {

            Set<String> processedBooks = loadProcessedBooks(PROCESSED_BOOKS_PATH);
            Set<String> concurrentProcessedBooks = ConcurrentHashMap.newKeySet();
            concurrentProcessedBooks.addAll(processedBooks);

            ExecutorService executor = Executors.newFixedThreadPool(4);

            System.out.println("Starting indexing");

            try {
                submitTask(executor, () -> {
                    Set<String> taskProcessedBooks = new HashSet<>(concurrentProcessedBooks);
                    controllerTree.processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_TREE, taskProcessedBooks);
                }, "TreeInvertedIndex");

                submitTask(executor, () -> {
                    Set<String> taskProcessedBooks = new HashSet<>(concurrentProcessedBooks); // Crear una copia
                    controllerUnique.processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_UNIQUE, taskProcessedBooks);
                }, "UniqueInvertedIndex");

                submitTask(executor, () -> {
                    Set<String> taskProcessedBooks = new HashSet<>(concurrentProcessedBooks); // Crear una copia
                    controllerHierarchical.processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_HIERARCHICAL, taskProcessedBooks);
                }, "HierarchicalInvertedIndex");

                submitTask(executor, () -> {
                    Set<String> taskProcessedBooks = new HashSet<>(concurrentProcessedBooks); // Crear una copia
                    controllerTree.processMetadata(BOOKS_DIRECTORY, METADATA_DIRECTORY, taskProcessedBooks);
                }, "MetadataProcessing");

                executor.shutdown();
                if (!executor.awaitTermination(60, TimeUnit.MINUTES)) {
                    System.err.println("Parallel processing timeout.");
                }

                Set<String> fileNames = getFileNamesFromDataLake(BOOKS_DIRECTORY);
                saveProcessedBooks(fileNames);
                System.out.println("Inverted Index and Metadata processed successfully.");
            } catch (InterruptedException e) {
                System.err.println("Error during parallel processing: " + e.getMessage());
                e.printStackTrace();
            } finally {
                executor.shutdown();
            }
        }, 0, 30, TimeUnit.MINUTES);
    }

    private static Set<String> loadProcessedBooks(String PROCESSED_BOOKS_PATH) {
        Set<String> processedBooks = new HashSet<>();
        File file = new File(PROCESSED_BOOKS_PATH);
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    processedBooks.add(line.trim());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return processedBooks;
    }

    private static void saveProcessedBooks(Set<String> processedBooks) {
        String PROCESSED_BOOKS_PATH = System.getenv("PROCESSED_BOOKS_PATH");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PROCESSED_BOOKS_PATH))) {
            for (String docId : processedBooks) {
                writer.write(docId);
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void submitTask(ExecutorService executor, Runnable task, String taskName) {
        executor.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                System.err.println("Error in " + taskName + ": " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static Set<String> getFileNamesFromDataLake(String directoryPath) {
        Set<String> fileNames = new HashSet<>();

        try {
            Files.walk(Paths.get(directoryPath))
                    .filter(Files::isRegularFile)
                    .forEach(path -> fileNames.add(path.getFileName().toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        return fileNames;
    }
}
