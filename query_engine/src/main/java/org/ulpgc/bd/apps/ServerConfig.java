package org.ulpgc.bd.apps;
import static spark.Spark.*;

public class ServerConfig {
    public static void configure() {
        port(8080);
        staticFiles.location("/public");
        threadPool(10);
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin", "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
        });
    }
}