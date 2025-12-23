package com.allo.strategy.impl;

import com.allo.dto.FrankfurterHistoricalResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoricalIdrUsdStrategyTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private HistoricalIdrUsdStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new HistoricalIdrUsdStrategy(webClient);
    }

    @Test
    void testGetResourceType() {
        assertEquals("historical_idr_usd", strategy.getResourceType());
    }

    @Test
    void testFetchData_Success() {
        // Arrange
        FrankfurterHistoricalResponse mockResponse = new FrankfurterHistoricalResponse();
        mockResponse.setBase("IDR");
        mockResponse.setStart_date("2024-01-01");
        mockResponse.setEnd_date("2024-01-05");
        Map<String, Map<String, Double>> rates = new HashMap<>();
        Map<String, Double> dayRates = new HashMap<>();
        dayRates.put("USD", 0.000064);
        rates.put("2024-01-01", dayRates);
        mockResponse.setRates(rates);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FrankfurterHistoricalResponse.class))
                .thenReturn(Mono.just(mockResponse));

        // Act
        FrankfurterHistoricalResponse result = (FrankfurterHistoricalResponse) strategy.fetchData().block();

        // Assert
        assertNotNull(result);
        assertEquals("IDR", result.getBase());
        assertEquals("2024-01-01", result.getStart_date());
        assertEquals("2024-01-05", result.getEnd_date());
        assertNotNull(result.getRates());
    }

    @Test
    void testFetchData_ErrorHandling() {
        // Arrange
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FrankfurterHistoricalResponse.class))
                .thenReturn(Mono.error(new RuntimeException("API error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            strategy.fetchData().block();
        });
    }
}

