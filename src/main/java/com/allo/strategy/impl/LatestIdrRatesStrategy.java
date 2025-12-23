package com.allo.strategy.impl;

import com.allo.dto.FrankfurterLatestResponse;
import com.allo.dto.LatestIdrRatesResponse;
import com.allo.strategy.IDRDataFetcher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
public class LatestIdrRatesStrategy implements IDRDataFetcher {

    private final WebClient webClient;
    private final String githubUsername;

    public LatestIdrRatesStrategy(WebClient webClient, 
                                   @Value("${github.username}") String githubUsername) {
        this.webClient = webClient;
        this.githubUsername = githubUsername;
    }

    @Override
    public Mono<Object> fetchData() {
        return webClient.get()
                .uri("/latest?base=IDR")
                .retrieve()
                .bodyToMono(FrankfurterLatestResponse.class)
                .map(this::transformResponse)
                .cast(Object.class)
                .onErrorResume(error -> {
                    throw new RuntimeException("Failed to fetch latest IDR rates: " + error.getMessage(), error);
                });
    }

    @Override
    public String getResourceType() {
        return "latest_idr_rates";
    }

    private LatestIdrRatesResponse transformResponse(FrankfurterLatestResponse response) {
        double spreadFactor = calculateSpreadFactor();
        Double usdRate = response.getRates().get("USD");
        
        if (usdRate == null || usdRate == 0) {
            throw new RuntimeException("USD rate not found or invalid in response");
        }
        
        double usdBuySpreadIdr = (1.0 / usdRate) * (1.0 + spreadFactor);
        
        return new LatestIdrRatesResponse(
                response.getBase(),
                response.getDate(),
                response.getRates(),
                usdBuySpreadIdr
        );
    }

    private double calculateSpreadFactor() {
        String lowerUsername = githubUsername.toLowerCase();
        int sum = 0;
        for (char c : lowerUsername.toCharArray()) {
            sum += (int) c;
        }
        return (sum % 1000) / 100000.0;
    }
}

