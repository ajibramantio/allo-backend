package com.allo.service;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryDataStore {

    private final Map<String, Object> dataStore = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;

    public void storeData(String resourceType, Object data) {
        dataStore.put(resourceType, data);
    }

    public Object getData(String resourceType) {
        if (!initialized) {
            throw new IllegalStateException("Data store has not been initialized yet");
        }
        return dataStore.get(resourceType);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void markAsInitialized() {
        this.initialized = true;
    }

    public Map<String, Object> getAllData() {
        return Collections.unmodifiableMap(dataStore);
    }
}

