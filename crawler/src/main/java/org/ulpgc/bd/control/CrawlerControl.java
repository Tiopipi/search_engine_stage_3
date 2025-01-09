package org.ulpgc.bd.control;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

public class CrawlerControl {

    private static final Logger logger = Logger.getLogger(CrawlerControl.class.getName());
    private final BookSupplier supplier;
    private final BookStore store;

    static {
        try {
            logger.addHandler(new FileHandler("crawler.log"));
        } catch (IOException e) {
            logger.severe("Failed to initialize logger or create directories: " + e.getMessage());
        }
    }

    public CrawlerControl(BookSupplier supplier, BookStore store) {
        this.supplier = supplier;
        this.store = store;
    }

    public Map<String, Integer> runCrawler(List<Integer> bookshelves, int numBooks, int startIndex, String repository, int currentBookshelf) {
        int totalBooksDownloaded = 0;
        Map<String, Integer> result = new HashMap<>();
        while (currentBookshelf < bookshelves.size()) {
            int downloadedInShelf = processBookshelf(bookshelves.get(currentBookshelf), startIndex, numBooks, repository);
            totalBooksDownloaded += downloadedInShelf;
            if (totalBooksDownloaded >= numBooks) {
                logger.info("Downloaded " + totalBooksDownloaded + " books.");
                result.put("currentBookshelf", currentBookshelf);
                result.put("currentIndex", startIndex + downloadedInShelf);
                return result;
            }
            currentBookshelf++;
            startIndex = 0;
        }
        logger.info("Downloaded a total of " + totalBooksDownloaded + " books from all bookshelves.");
        return finalizeResult(currentBookshelf);
    }

    private int processBookshelf(int bookshelf, int startIndex, int numBooks, String repository) {
        List<String> bookPageLinks = supplier.getBookPageLinks(String.valueOf(bookshelf), logger);
        if (bookPageLinks.isEmpty()) {
            logger.info("No book pages found in bookshelf: " + bookshelf);
            return 0;
        }

        return downloadBooksFromLinks(bookPageLinks, startIndex, numBooks, repository);
    }

    private int downloadBooksFromLinks(List<String> bookPageLinks, int startIndex, int numBooks, String repository) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        int booksDownloaded = 0;

        for (int i = startIndex; i < bookPageLinks.size(); i++) {
            String bookPageUrl = bookPageLinks.get(i);
            if (submitBookDownload(bookPageUrl, repository, executor)) {
                booksDownloaded++;
                if (booksDownloaded >= numBooks) {
                    executor.shutdown();
                    return booksDownloaded;
                }
            }
        }
        executor.shutdown();
        return booksDownloaded;
    }

    private boolean submitBookDownload(String bookPageUrl, String repository, ExecutorService executor) {
        String bookId = bookPageUrl.substring(bookPageUrl.lastIndexOf("/") + 1);
        String txtLink = supplier.getBookDownloadLink(bookPageUrl, logger);
        if (txtLink != null) {
            executor.submit(() -> store.storeBook(txtLink, bookId, repository, logger));
            return true;
        }
        return false;
    }

    private Map<String, Integer> finalizeResult(int currentBookshelf) {
        Map<String, Integer> result = new HashMap<>();
        result.put("currentBookshelf", currentBookshelf);
        result.put("currentIndex", -1);
        return result;
    }


}
