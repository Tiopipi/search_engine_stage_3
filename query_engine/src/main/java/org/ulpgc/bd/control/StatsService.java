package org.ulpgc.bd.control;

import org.ulpgc.bd.utils.stats.StatsGeneratorInterface;

import java.io.IOException;
import java.util.Map;

public class StatsService {
    private final StatsGeneratorInterface statsGenerator;

    public StatsService(StatsGeneratorInterface statsGenerator) {
        this.statsGenerator = statsGenerator;
    }

    public Map<String, Object> calculateStats(String type, Map<String, String> params) throws IOException {
        return statsGenerator.generateStatistics(type, params);
    }
}

