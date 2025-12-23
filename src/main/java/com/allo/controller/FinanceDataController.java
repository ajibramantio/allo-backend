package com.allo.controller;

import com.allo.service.InMemoryDataStore;
import com.allo.strategy.IDRDataFetcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/finance")
public class FinanceDataController {

    private final InMemoryDataStore dataStore;
    private final Map<String, IDRDataFetcher> strategyMap;

    @Autowired
    public FinanceDataController(InMemoryDataStore dataStore, List<IDRDataFetcher> strategies) {
        this.dataStore = dataStore;
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(IDRDataFetcher::getResourceType, strategy -> strategy));
    }

    @GetMapping("/data/{resourceType}")
    public ResponseEntity<?> getFinanceData(@PathVariable String resourceType) {
        IDRDataFetcher strategy = strategyMap.get(resourceType);
        if (strategy == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid resource type. Valid types are: " + strategyMap.keySet());
        }

        if (!dataStore.isInitialized()) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Data store is not yet initialized. Please wait for application startup to complete.");
        }

        try {
            Object data = dataStore.getData(resourceType);
            if (data == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Data not found for resource type: " + resourceType);
            }
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error retrieving data: " + e.getMessage());
        }
    }
}

