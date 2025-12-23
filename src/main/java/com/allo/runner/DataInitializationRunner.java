package com.allo.runner;

import com.allo.service.InMemoryDataStore;
import com.allo.strategy.IDRDataFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataInitializationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializationRunner.class);

    private final List<IDRDataFetcher> dataFetchers;
    private final InMemoryDataStore dataStore;

    @Autowired
    public DataInitializationRunner(List<IDRDataFetcher> dataFetchers, InMemoryDataStore dataStore) {
        this.dataFetchers = dataFetchers;
        this.dataStore = dataStore;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Starting data initialization on application startup...");
        
        Map<String, IDRDataFetcher> fetcherMap = dataFetchers.stream()
                .collect(Collectors.toMap(IDRDataFetcher::getResourceType, fetcher -> fetcher));

        for (IDRDataFetcher fetcher : dataFetchers) {
            try {
                logger.info("Fetching data for resource type: {}", fetcher.getResourceType());
                Object data = fetcher.fetchData().block();
                dataStore.storeData(fetcher.getResourceType(), data);
                logger.info("Successfully loaded data for resource type: {}", fetcher.getResourceType());
            } catch (Exception e) {
                logger.error("Failed to fetch data for resource type: {}", fetcher.getResourceType(), e);
                throw new RuntimeException("Data initialization failed for " + fetcher.getResourceType(), e);
            }
        }

        dataStore.markAsInitialized();
        logger.info("Data initialization completed successfully. All resources loaded into in-memory store.");
    }
}

