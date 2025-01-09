package org.ulpgc.bd.apps;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class HtmlOpener {

    public static void openHtmlInBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Error opening the browser: " + e.getMessage());
        }
    }

}
