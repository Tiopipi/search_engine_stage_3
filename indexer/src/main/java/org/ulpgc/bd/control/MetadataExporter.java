package org.ulpgc.bd.control;

import org.example.model.Metadata;

import java.util.List;

public interface MetadataExporter {
    void exportMetadata(List<Metadata> metadataList, String metadataDirectory);
}
