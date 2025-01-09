package org.ulpgc.bd.service.interfaces;

import org.example.model.Metadata;

import java.util.*;

public interface MetadataService {
    List<Metadata> searchMetadata(Map<String, String> filters);
}
