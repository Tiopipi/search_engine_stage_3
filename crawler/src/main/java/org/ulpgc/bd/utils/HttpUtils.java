package org.ulpgc.bd.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.logging.Logger;

public class HttpUtils {

    private HttpUtils() {
    }

    public static Document fetchDocument(String url, Logger logger) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            logger.warning("Error fetching document from URL: " + url + " - " + e.getMessage());
            return null;
        }
    }
}
