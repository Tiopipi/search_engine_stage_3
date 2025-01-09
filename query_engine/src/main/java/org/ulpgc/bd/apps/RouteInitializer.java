package org.ulpgc.bd.apps;

import static spark.Spark.get;

import org.example.model.Metadata;
import org.example.utils.*;
import org.example.view.InvertedIndexView;
import org.ulpgc.bd.control.*;
import org.ulpgc.bd.repository.implementation.*;
import org.ulpgc.bd.repository.interfaces.DatalakeLoaderInterface;
import org.ulpgc.bd.repository.interfaces.DocumentLoader;
import org.ulpgc.bd.repository.interfaces.InvertedIndexLoader;
import org.ulpgc.bd.service.implementation.*;
import org.ulpgc.bd.service.interfaces.*;
import org.ulpgc.bd.utils.batch.BatchProcessor;
import org.ulpgc.bd.utils.batch.BatchProcessorInterface;
import org.ulpgc.bd.utils.cache.*;
import org.ulpgc.bd.utils.hazelcast.*;
import org.ulpgc.bd.utils.metadata.*;
import org.ulpgc.bd.utils.singleProcessor.RecordProcessor;
import org.ulpgc.bd.utils.singleProcessor.RecordProcessorInterface;
import org.ulpgc.bd.utils.stats.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class RouteInitializer {

    public static void initializeControllers(List<Metadata> metadataList, CacheManagerInterface cacheManager,
                                             DocumentLoader documentLoader, String[] paths) {

        HazelcastNode hazelcastNode = HazelcastNode.getInstance();
        Serializer serializer = new BinarySerializer();
        RecordProcessorInterface recordProcessor = new RecordProcessor(paths[4],paths[3],serializer);
        BatchProcessorInterface batchProcessor = new BatchProcessor(recordProcessor);
        DatalakeLoaderInterface datalakeLoader = new DatalakeLoader(paths[4], hazelcastNode,batchProcessor);
        QueryParameterExtractor queryParameterExtractor = new QueryParameterExtractor();
        TransformerService transformerService = new TransformerService();
        MetadataService metadataService = new MetadataServiceImplementation(metadataList);
        MetadataFilterInterface metadataFilter = new MetadataFilter(metadataService);
        DocumentFilterInterface documentFilter = new DocumentFilterService(cacheManager, documentLoader);
        QueryProcessorInterface queryProcessor = createQueryProcessor(transformerService, documentFilter);

        configureSearchRoutes(metadataList, paths, queryProcessor, cacheManager, metadataFilter, datalakeLoader, queryParameterExtractor);

        configureDocumentAndStatsRoutes(metadataList, paths, queryProcessor, cacheManager, metadataFilter, datalakeLoader, queryParameterExtractor);

        setupBookRoute(paths[4]);
    }

    private static void configureSearchRoutes(List<Metadata> metadataList, String[] paths,
                                              QueryProcessorInterface queryProcessor, CacheManagerInterface cacheManager,
                                              MetadataFilterInterface metadataFilter, DatalakeLoaderInterface datalakeLoader,
                                              QueryParameterExtractor queryParameterExtractor) {

        String[] indexTypes = {"unique", "tree", "hierarchical"};

        Arrays.stream(indexTypes)
                .forEach(type -> {
                    InvertedIndexLoader loader = createInvertedIndexLoader(type, paths);
                    StatsGeneratorInterface statsGenerator = new StatsGenerator(datalakeLoader, loader, metadataList);
                    QueryService queryService = createQueryService(queryProcessor, cacheManager, metadataFilter, statsGenerator, loader);
                    StatsService statsService = new StatsService(statsGenerator);
                    QueryResponseHandler queryResponseHandler = new QueryResponseHandler(queryService, statsService, queryParameterExtractor);
                    QueryController queryController = new QueryController(queryParameterExtractor, queryResponseHandler);
                    get("/search/" + type + "/combined", queryController.searchClient);
                });
    }

    private static void configureDocumentAndStatsRoutes(List<Metadata> metadataList, String[] paths,
                                                        QueryProcessorInterface queryProcessor, CacheManagerInterface cacheManager,
                                                        MetadataFilterInterface metadataFilter, DatalakeLoaderInterface datalakeLoader,
                                                        QueryParameterExtractor queryParameterExtractor) {

        StatsGeneratorInterface statsGenerator = new StatsGenerator(datalakeLoader,
                new UniqueInvertedIndexLoader(paths[1], new InvertedIndexView(new BinarySerializer())),
                metadataList);
        QueryService queryService = createQueryService(queryProcessor, cacheManager, metadataFilter, statsGenerator, null);
        StatsService statsService = new StatsService(statsGenerator);
        QueryResponseHandler queryResponseHandler = new QueryResponseHandler(queryService, statsService, queryParameterExtractor);
        QueryController queryController = new QueryController(queryParameterExtractor, queryResponseHandler);

        get("/documents/:words", queryController.searchClientHazelcast);
        get("/stats/:type", queryController.searchStats);
    }

    private static QueryProcessorInterface createQueryProcessor(TransformerService transformerService,
                                                                DocumentFilterInterface documentFilter) {
        return new QueryProcessor(
                new InvertedIndexServiceImplementation(new ResultEnricher()),
                documentFilter,
                transformerService
        );
    }

    private static QueryService createQueryService(QueryProcessorInterface queryProcessor, CacheManagerInterface cacheManager,
                                                   MetadataFilterInterface metadataFilter, StatsGeneratorInterface statsGenerator,
                                                   InvertedIndexLoader loader) {
        return new QueryService(queryProcessor, cacheManager, metadataFilter, statsGenerator, loader);
    }

    private static InvertedIndexLoader createInvertedIndexLoader(String type, String[] paths) {
        InvertedIndexView view = new InvertedIndexView(new BinarySerializer());
        return switch (type) {
            case "unique" -> new UniqueInvertedIndexLoader(paths[1], view);
            case "tree" -> new TreeDataStructureInvertedIndexLoader(paths[2], view);
            default -> new HierarchicalInvertedIndexLoader(paths[3], view);
        };
    }

    public static void setupBookRoute(String booksPath) {
        get("/books/:filename", (req, res) -> {
            Path filePath = Paths.get(booksPath, req.params(":filename"));
            if (Files.exists(filePath)) {
                res.type(Files.probeContentType(filePath));
                return Files.readAllBytes(filePath);
            } else {
                res.status(404);
                return "{\"error\":\"File not found\"}";
            }
        });
    }
}
