package org.ulpgc.bd.apps;

import org.example.utils.BinarySerializer;
import org.example.view.InvertedIndexView;
import org.ulpgc.bd.repository.implementation.*;
import org.ulpgc.bd.repository.interfaces.DocumentLoader;
import org.ulpgc.bd.utils.batch.BatchProcessor;
import org.ulpgc.bd.utils.batch.BatchProcessorInterface;
import org.ulpgc.bd.utils.cache.*;
import org.ulpgc.bd.utils.extractor.*;

import org.ulpgc.bd.utils.hazelcast.*;
import org.example.model.Metadata;
import org.example.utils.Serializer;
import org.ulpgc.bd.utils.singleProcessor.RecordProcessor;
import org.ulpgc.bd.utils.singleProcessor.RecordProcessorInterface;

import java.io.IOException;
import java.util.*;

public class MainApplication {
    public static void main(String[] args) {
        ServerConfig.configure();

        String[] paths = EnvironmentLoader.loadPaths();

        HazelcastNode hazelcastNode = HazelcastNode.getInstance();
        Serializer serializer = new BinarySerializer();
        RecordProcessorInterface recordProcessor = new RecordProcessor(paths[4],paths[3],serializer);
        BatchProcessorInterface batchProcessor = new BatchProcessor(recordProcessor);
        CsvMetadataLoader csvMetadataLoader = new CsvMetadataLoader(hazelcastNode,batchProcessor);
        CacheManagerInterface cacheManager = new CacheManager(hazelcastNode);
        DocumentLoader documentLoader = new TextDocumentLoader(paths[4]);

        DataInitializer dataInitializer = new DataInitializer(hazelcastNode, csvMetadataLoader, batchProcessor);
        ExtractorInterface booksExtractor = new Extractor(paths[0], new InvertedIndexView(serializer));

        dataInitializer.initializeSet(booksExtractor.extractBookIdsFromCsv(paths[0]),"metadataSet");
        dataInitializer.initializeSet(booksExtractor.extractBookIdsFromCsv(paths[0]), "datalakeSet");
        try {
            ExtractorInterface wordsExtractor = new Extractor(paths[1], new InvertedIndexView(serializer));
            List<String> allWords = wordsExtractor.extractAllWords();
            dataInitializer.initializeSet(new ArrayList<>(allWords), "datamartSet");
        } catch (IOException e) {
            e.printStackTrace();
        }
        dataInitializer.loadDatamart(paths[3]);
        dataInitializer.loadDatalake(paths[4]);

        dataInitializer.loadMetadata(paths[0]);

        
        List<Metadata> metadataList= csvMetadataLoader.convertIMapToMetadataList(hazelcastNode.getMetadataIMap());



        RouteInitializer.initializeControllers(metadataList, cacheManager, documentLoader, paths);
        System.out.println("Server running on port 8080...");
        HtmlOpener.openHtmlInBrowser("http://localhost:8080/gui.html");
    }


}
