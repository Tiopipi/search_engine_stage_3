package org.ulpgc.bd.repository.interfaces;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface DatalakeLoaderInterface {
    void loadDatalake();
    Stream<Path> loadBooksPath() throws IOException;
}
