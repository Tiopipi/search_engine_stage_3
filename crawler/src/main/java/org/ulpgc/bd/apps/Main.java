package org.ulpgc.bd.apps;

import org.ulpgc.bd.control.CrawlerControl;
import org.ulpgc.bd.implementation.GutenbergSupplier;
import org.ulpgc.bd.implementation.TextFormatStore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {

    private static final Logger logger = Logger.getLogger(Main.class.getName());
    private static int currentBookshelf = 0;
    private static int startIndex = 0;
    private static List<Integer> bookshelves = new ArrayList<>();

    public static void main(String[] args) {
        String repositoryDocuments = getRepositoryDocumentsPath();
        initializeBookshelves();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        TextFormatStore textFormatStore = new TextFormatStore();
        GutenbergSupplier gutenbergSupplier = new GutenbergSupplier();
        CrawlerControl crawlerControl = new CrawlerControl(gutenbergSupplier, textFormatStore);

        startCrawlerScheduler(scheduler, crawlerControl, repositoryDocuments);
    }

    private static String getRepositoryDocumentsPath() {
        return System.getenv("REPOSITORY_DOCUMENTS");
    }

    private static void initializeBookshelves() {
        for (int i = 5; i <= 487; i++) {
            bookshelves.add(i);
        }
    }

    private static void startCrawlerScheduler(ScheduledExecutorService scheduler, CrawlerControl crawlerControl, String repositoryDocuments) {
        scheduler.scheduleAtFixedRate(() -> processBookshelves(scheduler, crawlerControl, repositoryDocuments),
                0, 30, TimeUnit.MINUTES);
    }

    private static void processBookshelves(ScheduledExecutorService scheduler, CrawlerControl crawlerControl, String repositoryDocuments) {
        if (currentBookshelf >= bookshelves.size()) {
            logAndShutdownScheduler(scheduler, "There are no more libraries left to process. Ending execution");
        } else {
            processCurrentBookshelf(crawlerControl, repositoryDocuments, scheduler);
        }
    }

    private static void processCurrentBookshelf(CrawlerControl crawlerControl, String repositoryDocuments, ScheduledExecutorService scheduler) {
        logger.info("Starting crawler for bookshelf number: " + currentBookshelf);

        Map<String, Integer> result = crawlerControl.runCrawler(
                bookshelves, 2150, startIndex, repositoryDocuments, currentBookshelf);

        if (startIndex == -1) {
            logAndShutdownScheduler(scheduler, "Completed processing all libraries");
        } else {
            updateState(result);
        }
    }

    private static void updateState(Map<String, Integer> result) {
        currentBookshelf = result.get("currentBookshelf");
        startIndex = result.get("currentIndex");
    }

    private static void logAndShutdownScheduler(ScheduledExecutorService scheduler, String message) {
        logger.info(message);
        scheduler.shutdown();
    }
}
