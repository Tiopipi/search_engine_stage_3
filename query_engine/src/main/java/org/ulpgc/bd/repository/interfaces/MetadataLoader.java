package org.ulpgc.bd.repository.interfaces;

import com.hazelcast.map.IMap;
import com.hazelcast.multimap.MultiMap;
import org.example.model.Metadata;

import java.util.List;

public interface MetadataLoader {
    void loadMetadata(String METADATA_FILE_PATH);
    List<Metadata> convertIMapToMetadataList(IMap<String, List<String>> metadataMap);
}
