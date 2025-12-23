package com.allo.strategy.impl;

import com.allo.dto.FrankfurterCurrenciesResponse;
import com.allo.strategy.IDRDataFetcher;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class SupportedCurrenciesStrategy implements IDRDataFetcher {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    public SupportedCurrenciesStrategy(WebClient webClient) {
        this.webClient = webClient;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Mono<Object> fetchData() {
        return webClient.get()
                .uri("/currencies")
                .retrieve()
                .bodyToMono(String.class)
                .map(json -> {
                    try {
                        Map<String, String> currenciesMap = objectMapper.readValue(json, new TypeReference<Map<String, String>>() {});
                        FrankfurterCurrenciesResponse response = new FrankfurterCurrenciesResponse();
                        response.setCurrencies(currenciesMap);
                        return response;
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to parse currencies response", e);
                    }
                })
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

