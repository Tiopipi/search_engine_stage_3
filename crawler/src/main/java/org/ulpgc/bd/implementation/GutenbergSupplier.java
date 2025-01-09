package org.ulpgc.bd.implementation;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.ulpgc.bd.control.BookSupplier;
import org.ulpgc.bd.utils.HttpUtils;
import org.ulpgc.bd.utils.UrlUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GutenbergSupplier implements BookSupplier {

    private static final String BASE_URL = "https://www.gutenberg.org/";

    @Override
    public List<String> getBookPageLinks(String bookshelfNum, Logger logger) {
        String categoryUrl = BASE_URL + "ebooks/bookshelf/" + bookshelfNum;
        List<String> bookPageLinks = new ArrayList<>();
        String currentPage = categoryUrl;
        while (currentPage != null) {
            Document doc = HttpUtils.fetchDocument(currentPage, logger);
            if (doc == null) break;

            bookPageLinks.addAll(extractBookLinks(doc));
            currentPage = getNextPageUrl(doc);
        }
        logger.info("Links to book pages found: " + bookPageLinks.size());
        return bookPageLinks;
    }

    @Override
    public String getBookDownloadLink(String bookPageUrl, Logger logger) {
        try {
            String bookId = extractBookId(bookPageUrl);
            String txtUrl = buildTxtUrl(bookId);
            return UrlUtils.isUrlAvailable(txtUrl) ? txtUrl : null;
        } catch (IOException e) {
            logger.warning("Error getting book download link: " + e.getMessage());
            return null;
        }
    }

    private List<String> extractBookLinks(Document doc) {
        List<String> links = new ArrayList<>();
        Elements elements = doc.select("a[href]");
        for (Element link : elements) {
            String href = link.attr("href");
            if (href.startsWith("/ebooks/") && href.substring(8).matches("\\d+")) {
                links.add(BASE_URL + href);
            }
        }
        return links;
    }

    private String getNextPageUrl(Document doc) {
        Element nextButton = doc.select("a:contains(Next)").first();
        return (nextButton != null) ? BASE_URL + nextButton.attr("href") : null;
    }

    private String extractBookId(String bookPageUrl) {
        return bookPageUrl.substring(bookPageUrl.lastIndexOf("/") + 1);
    }

    private String buildTxtUrl(String bookId) {
        return "https://www.gutenberg.org/cache/epub/" + bookId + "/pg" + bookId + ".txt";
    }
}
