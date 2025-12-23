package com.allo.strategy.impl;

import com.allo.dto.FrankfurterLatestResponse;
import com.allo.dto.LatestIdrRatesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LatestIdrRatesStrategyTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private LatestIdrRatesStrategy strategy;
    private static final String GITHUB_USERNAME = "aji";

    @BeforeEach
    void setUp() {
        strategy = new LatestIdrRatesStrategy(webClient, GITHUB_USERNAME);
    }

    @Test
    void testGetResourceType() {
        assertEquals("latest_idr_rates", strategy.getResourceType());
    }

    @Test
    void testFetchData_Success() {
        // Arrange
        FrankfurterLatestResponse mockResponse = new FrankfurterLatestResponse();
        mockResponse.setBase("IDR");
        mockResponse.setDate("2024-01-15");
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 0.000064);
        rates.put("EUR", 0.000059);
        mockResponse.setRates(rates);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FrankfurterLatestResponse.class))
                .thenReturn(Mono.just(mockResponse));

        // Act
        LatestIdrRatesResponse result = (LatestIdrRatesResponse) strategy.fetchData().block();

        // Assert
        assertNotNull(result);
        assertEquals("IDR", result.getBase());
        assertEquals("2024-01-15", result.getDate());
        assertNotNull(result.getRates());
        assertNotNull(result.getUsdBuySpreadIdr());
        assertTrue(result.getUsdBuySpreadIdr() > 0);
        
        // Verify spread calculation: (1 / 0.000064) * (1 + spreadFactor)
        // spreadFactor = (sum of 'aji' ASCII % 1000) / 100000
        // 'a'=97, 'j'=106, 'i'=105, sum=308, 308%1000=308, 308/100000=0.00308
        double expectedBase = 1.0 / 0.000064; // 15625
        double spreadFactor = 0.00308;
        double expectedSpread = expectedBase * (1.0 + spreadFactor);
        assertEquals(expectedSpread, result.getUsdBuySpreadIdr(), 0.01);
    }

    @Test
    void testFetchData_ErrorHandling() {
        // Arrange
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(FrankfurterLatestResponse.class))
                .thenReturn(Mono.error(new RuntimeException("Network error")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            strategy.fetchData().block();
        });
    }

    @Test
    void testSpreadFactorCalculation() {
        // Test the spread factor calculation logic
        String username = "aji";
        int sum = 0;
        for (char c : username.toLowerCase().toCharArray()) {
            sum += (int) c;
        }
        double spreadFactor = (sum % 1000) / 100000.0;
        
        // 'a'=97, 'j'=106, 'i'=105, sum=308
        assertEquals(308, sum);
        assertEquals(0.00308, spreadFactor, 0.00001);
    }
}

