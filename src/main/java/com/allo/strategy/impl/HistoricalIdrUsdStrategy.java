package com.allo.strategy.impl;

import com.allo.dto.FrankfurterHistoricalResponse;
import com.allo.strategy.IDRDataFetcher;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class HistoricalIdrUsdStrategy implements IDRDataFetcher {

    private final WebClient webClient;
    private static final String DATE_RANGE = "2024-01-01..2024-01-05";

    public HistoricalIdrUsdStrategy(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Object> fetchData() {
        return webClient.get()
                .uri("/{dateRange}?from=IDR&to=USD", DATE_RANGE)
                .retrieve()
                .bodyToMono(FrankfurterHistoricalResponse.class)
                .cast(Object.class)
                .onErrorResume(error -> {
                    throw new RuntimeException("Failed to fetch historical IDR-USD data: " + error.getMessage(), error);
                });
    }

    @Override
    public String getResourceType() {
        return "historical_idr_usd";
    }
}

