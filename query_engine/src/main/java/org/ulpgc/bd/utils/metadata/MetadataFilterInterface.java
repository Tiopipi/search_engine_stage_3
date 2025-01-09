package org.ulpgc.bd.utils.metadata;

import org.example.model.Metadata;

import java.util.*;

public interface MetadataFilterInterface {
    List<Metadata> getFilteredAndDateRangeMetadata(Map<String, String> params);

}
