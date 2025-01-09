package org.ulpgc.bd.utils.hazelcast;

import com.hazelcast.collection.IQueue;
import com.hazelcast.config.*;
import com.hazelcast.core.*;
import com.hazelcast.map.IMap;
import com.hazelcast.cp.IAtomicLong;

import java.util.List;

public class HazelcastNode {
    private static HazelcastNode instance;
    private final HazelcastInstance hazelcastInstance;

    private HazelcastNode() {
        Config config = new Config();
        config.setClusterName("dev");

        NetworkConfig networkConfig = config.getNetworkConfig();

        networkConfig.getJoin().getMulticastConfig().setEnabled(false);

        networkConfig.getJoin().getTcpIpConfig()
                .setEnabled(true)
                .addMember("10.26.14.239:5701")
                .addMember("10.26.14.243:5701")
                .addMember("10.26.14.244:5701")
                .addMember("10.26.14.242:5701")
                .addMember("10.26.14.241:5701")
                .addMember("10.195.135.45:5701")
                .addMember("10.26.14.230:5701");

        config.addMapConfig(createIMapConfig("DatamartCache"));
        config.addMapConfig(createIMapConfig("DatalakeCache"));
        config.addMultiMapConfig(createMultiMapConfig("MetadataCache"));

        this.hazelcastInstance = Hazelcast.newHazelcastInstance(config);

        initializeAtomicCounter("metadataSetInitialized");
        initializeAtomicCounter("datalakeSetInitialized");
        initializeAtomicCounter("datamartSetInitialized");
    }

    private MapConfig createIMapConfig(String iMapName) {
        MapConfig mapConfig = new MapConfig(iMapName);

        mapConfig.setTimeToLiveSeconds(0);

        mapConfig.setBackupCount(1)
                .setAsyncBackupCount(1)
                .setReadBackupData(true);

        NearCacheConfig nearCacheConfig = new NearCacheConfig()
                .setName(iMapName)
                .setTimeToLiveSeconds(600)
                .setMaxIdleSeconds(300)
                .setInMemoryFormat(InMemoryFormat.BINARY)
                .setInvalidateOnChange(true)
                .setEvictionConfig(
                        new EvictionConfig()
                                .setEvictionPolicy(EvictionPolicy.LRU)
                                .setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT)
                                .setSize(1000)
                );

        mapConfig.setNearCacheConfig(nearCacheConfig);

        return mapConfig;
    }

    public static synchronized HazelcastNode getInstance() {
        if (instance == null) {
            instance = new HazelcastNode();
        }
        return instance;
    }

    public HazelcastInstance getHazelcastInstance() {
        return hazelcastInstance;
    }

    public IMap<String, String> getDatamartMap() {
        return hazelcastInstance.getMap("DatamartCache");
    }

    public IMap<String, String> getDatalakeMap() {
        return hazelcastInstance.getMap("DatalakeCache");
    }

    public IMap<String, List<String>> getMetadataIMap() {
        return hazelcastInstance.getMap("MetadataCache");
    }

    public void shutdown() {
        hazelcastInstance.shutdown();
    }

    public IQueue<String> getQueue(String name) {
        return hazelcastInstance.getQueue(name);
    }

    public IQueue<String> getMetadataLoadedSet() {
        return getQueue("metadataSet");
    }

    public IQueue<String> getDatalakeLoadedSet() {
        return getQueue("datalakeSet");
    }

    public IQueue<String> getDatamartLoadedSet() {
        return getQueue("datamartSet");
    }


    public IAtomicLong getAtomicCounter(String name) {
        return hazelcastInstance.getCPSubsystem().getAtomicLong(name);
    }

    private MultiMapConfig createMultiMapConfig(String multiMapName) {
        MultiMapConfig multiMapConfig = new MultiMapConfig(multiMapName);
        multiMapConfig.setValueCollectionType(MultiMapConfig.ValueCollectionType.LIST);
        return multiMapConfig;
    }

    private void initializeAtomicCounter(String counterName) {
        hazelcastInstance.getCPSubsystem().getAtomicLong(counterName).compareAndSet(0, 0);
    }
}
