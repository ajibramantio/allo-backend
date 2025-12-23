package com.allo.runner;

import com.allo.service.InMemoryDataStore;
import com.allo.strategy.IDRDataFetcher;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "frankfurter.api.base-url=https://api.frankfurter.app",
        "github.username=aji"
})
class DataInitializationRunnerIntegrationTest {

    @Autowired
    private InMemoryDataStore dataStore;

    @Autowired
    private List<IDRDataFetcher> strategies;

    @Test
    void testDataStoreInitialized() {
        assertTrue(dataStore.isInitialized(), "Data store should be initialized after ApplicationRunner");
    }

    @Test
    void testAllResourcesLoaded() {
        assertNotNull(dataStore.getData("latest_idr_rates"), 
                "Latest IDR rates should be loaded");
        assertNotNull(dataStore.getData("historical_idr_usd"), 
                "Historical IDR-USD data should be loaded");
        assertNotNull(dataStore.getData("supported_currencies"), 
                "Supported currencies should be loaded");
    }

    @Test
    void testAllStrategiesRegistered() {
        assertEquals(3, strategies.size(), 
                "All three strategies should be registered");
        
        long uniqueResourceTypes = strategies.stream()
                .map(IDRDataFetcher::getResourceType)
                .distinct()
                .count();
        assertEquals(3, uniqueResourceTypes, 
                "All three resource types should be unique");
    }

    @Test
    void testDataStoreThreadSafety() throws InterruptedException {
        // Test that multiple threads can read from the data store safely
        Thread[] threads = new Thread[10];
        boolean[] results = new boolean[10];
        
        for (int i = 0; i < 10; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    Object data = dataStore.getData("latest_idr_rates");
                    results[index] = (data != null);
                } catch (Exception e) {
                    results[index] = false;
                }
            });
        }
        
        for (Thread thread : threads) {
            thread.start();
        }
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        for (boolean result : results) {
            assertTrue(result, "All threads should successfully read from data store");
        }
    }
}

