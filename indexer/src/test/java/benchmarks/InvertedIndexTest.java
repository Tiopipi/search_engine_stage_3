package benchmarks;

import org.openjdk.jmh.annotations.*;
import org.ulpgc.bd.control.*;
import org.ulpgc.bd.implementation.*;
import org.example.model.Document;
import org.example.utils.JsonSerializer;
import org.example.utils.Serializer;
import org.example.view.InvertedIndexView;
import org.example.model.InvertedIndex;

import java.nio.file.Path;
import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 10, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class InvertedIndexTest {

    private final String BOOKS_DIRECTORY = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_3/Datalake/eventstore/gutenbrg";
    private final String STOP_WORDS = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_stage_3/indexer/src/main/resources/Stop_words.txt";
    private final String INDEX_DIRECTORY_TREE = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_3/Datamarts/Inverted Index/Tree Data Structure";
    private final String INDEX_DIRECTORY_UNIQUE = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_3/Datamarts/Inverted Index/Unique Data Structure";
    private final String INDEX_DIRECTORY_HIERARCHICAL = "/Users/tiopipi/Desktop/universidad/3º Datos/Big Data/Search_engine_Stage_3/Datamarts/Inverted Index/Hierarchical Data Structure";

    private static final Logger logger = Logger.getLogger(InvertedIndexController.class.getName());
    private InvertedIndexManager uniqueInvertedIndexManager;
    private InvertedIndexManager treeInvertedIndexManager;
    private InvertedIndexManager hierarchicalInvertedIndexManager;
    private MetadataLoader metadataLoader;
    private MetadataExporter metadataExporter;
    private Set<String> processedBooks;
    private Set<String> stopWords;
    private BookLoader bookLoader;

    @Setup(Level.Iteration)
    public void setup() throws IOException {
        bookLoader = new BookFileLoader();
        bookLoader.initializeStopWords(STOP_WORDS);
        stopWords = bookLoader.getStopWords();

        processedBooks = new HashSet<>();
        Serializer serializer = new JsonSerializer();
        InvertedIndexView view = new InvertedIndexView(serializer);


        metadataLoader = new BookMetadataLoader();
        metadataExporter = new CsvMetadataExporter();
        uniqueInvertedIndexManager = new UniqueInvertedIndexManager(view);
        treeInvertedIndexManager = new TreeInvertedIndexManager(view);
        hierarchicalInvertedIndexManager = new HierarchicalInvertedIndexManager(view);

        new File(INDEX_DIRECTORY_TREE).mkdirs();
        new File(INDEX_DIRECTORY_UNIQUE).mkdirs();
        new File(INDEX_DIRECTORY_HIERARCHICAL).mkdirs();
    }

    @Benchmark
    public void benchmarkUniqueJsonInvertedIndexExport() {
        processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_UNIQUE, processedBooks, uniqueInvertedIndexManager);
    }

    @Benchmark
    public void benchmarkTreeInvertedIndexExport() {
        processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_TREE, processedBooks, treeInvertedIndexManager);    }

    @Benchmark
    public void benchmarkHierarchicalInvertedIndexExport() {
        processInvertedIndex(BOOKS_DIRECTORY, STOP_WORDS, INDEX_DIRECTORY_HIERARCHICAL, processedBooks, hierarchicalInvertedIndexManager);    }


    private void processInvertedIndex(String booksDirectory, String stopWordsPath, String indexDirectory, Set<String> processedBooks, InvertedIndexManager invertedIndexManager) {
        try {
            bookLoader.initializeStopWords(stopWordsPath);

            try (Stream<Path> bookPathsStream = bookLoader.getBookPaths(booksDirectory, processedBooks)) {
                List<Path> bookPaths = bookPathsStream.toList(); // Convertimos el stream a una lista
                final int BATCH_SIZE = 100;
                List<Document> batch = new ArrayList<>();
                List<String> processedBatch = new ArrayList<>();

                for (Path bookPath : bookPaths) {
                    try {
                        String bookId = extractBookId(bookPath);
                        if (!processedBooks.contains(bookId)) {
                            Document document = bookLoader.loadBook(bookPath);
                            if (document != null) {
                                batch.add(document);
                                processedBatch.add(bookId);
                            }
                        }

                        // Procesar lote completo
                        if (batch.size() >= BATCH_SIZE) {
                            processBatch(batch, processedBooks, indexDirectory, invertedIndexManager);
                            processedBooks.addAll(processedBatch);
                            batch.clear();
                            processedBatch.clear();
                        }
                    } catch (Exception e) {
                        logger.severe("Error loading or processing book: " + bookPath + ", error: " + e.getMessage());
                    }
                }

                // Procesar lote restante (< 100 libros)
                if (!batch.isEmpty()) {
                    logger.info("Processing remaining batch of " + batch.size() + " documents.");
                    processBatch(batch, processedBooks, indexDirectory, invertedIndexManager);
                    processedBooks.addAll(processedBatch);
                } else {
                    logger.info("No remaining documents to process.");
                }
            }
            processedBooks.clear();
        } catch (Exception e) {
            logger.severe("Error initializing book processing: " + e.getMessage());
        }
    }



    private void processBatch(List<Document> batch, Set<String> processedBooks, String indexDirectory, InvertedIndexManager invertedIndexManager) {
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

        } catch (Exception ignored) {
        }
    }
    private String extractBookId(Path bookPath) {
        return bookPath.getFileName().toString().replaceFirst("\\.txt$", "");
    }

}