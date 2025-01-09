package org.ulpgc.bd.utils.stats;

import java.io.IOException;
import java.util.Map;

public interface StatsGeneratorInterface {
    Map<String, Object> generateStatistics(String type, Map<String, String> params) throws IOException;
    void updateSearchFrequency(String query);
}
