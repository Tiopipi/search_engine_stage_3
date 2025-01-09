package org.ulpgc.bd.utils.extractor;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface ExtractorInterface {
    List<String> extractAllWords() throws IOException;
    List<String> extractBookIdsFromCsv(String metadataFilePath);
}
