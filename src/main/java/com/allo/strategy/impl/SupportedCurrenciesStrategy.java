package com.allo.strategy.impl;

import com.allo.dto.FrankfurterCurrenciesResponse;
import com.allo.strategy.IDRDataFetcher;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class SupportedCurrenciesStrategy implements IDRDataFetcher {

    private final WebClient webClient;

    public SupportedCurrenciesStrategy(WebClient webClient) {
        this.webClient = webClient;
    }

    @Override
    public Mono<Object> fetchData() {
        return webClient.get()
                .uri("/currencies")
                .retrieve()
                .bodyToMono(FrankfurterCurrenciesResponse.class)
                .cast(Object.class)
                .onErrorResume(error -> {
                    throw new RuntimeException("Failed to fetch supported currencies: " + error.getMessage(), error);
                });
    }

    @Override
    public String getResourceType() {
        return "supported_currencies";
    }
}

