package com.juicer.core;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author SkyJourney
 */
public class RuntimeStorage {

    private Map<String , Map<URL, JuicerTask>> juicerTaskQueue;

    private Map<String, List<JuicerData>> juicerResultStorage;

    private Map<String, String> juicerChain;

    private RuntimeStorage() {
        juicerTaskQueue = new ConcurrentHashMap<>();
        juicerResultStorage = new ConcurrentHashMap<>();
        juicerChain = new ConcurrentHashMap<>();
    }

    public Map<String, Map<URL, JuicerTask>> getJuicerTaskQueue() {
        return juicerTaskQueue;
    }

    public Map<String, List<JuicerData>> getJuicerResultStorage() {
        return juicerResultStorage;
    }

    public Map<String, String> getJuicerChain() {
        return juicerChain;
    }

    private static class SingletonHolder {
        private static final RuntimeStorage INSTANCE = new RuntimeStorage();
    }

    public static RuntimeStorage getInstance() {
        return SingletonHolder.INSTANCE;
    }


}
