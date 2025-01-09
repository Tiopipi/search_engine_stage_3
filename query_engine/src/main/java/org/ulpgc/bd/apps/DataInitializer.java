package org.ulpgc.bd.apps;

import com.hazelcast.collection.IQueue;
import com.hazelcast.cp.IAtomicLong;
import org.ulpgc.bd.repository.implementation.*;
import org.ulpgc.bd.utils.batch.BatchProcessorInterface;
import org.ulpgc.bd.utils.hazelcast.*;

import java.util.List;

public class DataInitializer {
    private final HazelcastNode hazelcastNode;
    private final CsvMetadataLoader metadataLoader;
    private BatchProcessorInterface batchProcessor;


    public DataInitializer(HazelcastNode hazelcastNode, CsvMetadataLoader metadataLoader, BatchProcessorInterface batchProcessor) {
        this.hazelcastNode = hazelcastNode;
        this.metadataLoader = metadataLoader;
        this.batchProcessor = batchProcessor;
    }

    public void loadMetadata(String metadataPath) {
        metadataLoader.loadMetadata(metadataPath);
        System.out.println("Metadata Stored in Hazelcast Cache");
    }

    public void loadDatamart(String datamartPath) {
        var datamartLoader = new DatamartLoader(hazelcastNode, batchProcessor);
        datamartLoader.loadDatamart();
        System.out.println("Datamart Stored in Hazelcast Cache");
    }

    public void loadDatalake(String datalakePath) {
        var datalakeLoader = new DatalakeLoader(datalakePath, hazelcastNode, batchProcessor);
        datalakeLoader.loadDatalake();
    }

    public void initializeSet(List<String> items, String setName) {
        IAtomicLong initializedFlag = hazelcastNode.getAtomicCounter(setName + "Initialized");

        if (initializedFlag.compareAndSet(0, 1)) {
            IQueue<String> set = hazelcastNode.getQueue(setName);
            set.addAll(items);
            System.out.println("Set " + setName + " initialized in Hazelcast.");
            notifyInitialization(initializedFlag);
        } else {
            waitForInitialization(initializedFlag, setName);
        }
    }


    private synchronized void waitForInitialization(IAtomicLong initializedFlag, String setName) {
        try {
            System.out.println("Waiting for initialization of set: " + setName);
            while (initializedFlag.get() == 0) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted while waiting for initialization of set: " + setName);
        }
        System.out.println("Set " + setName + " initialized, proceeding with other operations.");
    }

    public synchronized void notifyInitialization(IAtomicLong initializedFlag) {
        if (initializedFlag.get() == 1) {
            notifyAll();
        }
    }

}
