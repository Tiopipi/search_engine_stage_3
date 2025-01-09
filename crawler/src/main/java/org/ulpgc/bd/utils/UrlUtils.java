package org.ulpgc.bd.utils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlUtils {

    private UrlUtils() {
    }

    public static boolean isUrlAvailable(String url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("HEAD");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
        return connection.getResponseCode() == HttpURLConnection.HTTP_OK;
    }
}
