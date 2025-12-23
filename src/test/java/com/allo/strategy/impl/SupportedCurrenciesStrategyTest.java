package com.allo.strategy.impl;

import com.allo.dto.FrankfurterCurrenciesResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportedCurrenciesStrategyTest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private SupportedCurrenciesStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new SupportedCurrenciesStrategy(webClient);
    }

    @Test
    void testGetResourceType() {
        assertEquals("supported_currencies", strategy.getResourceType());
    }

    @Test
    void testFetchData_Success() {
        // Arrange
        String jsonResponse = "{\"USD\":\"United States Dollar\",\"EUR\":\"Euro\",\"IDR\":\"Indonesian Rupiah\"}";

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.just(jsonResponse));

        // Act
        FrankfurterCurrenciesResponse result = (FrankfurterCurrenciesResponse) strategy.fetchData().block();

        // Assert
        assertNotNull(result);
        assertNotNull(result.getCurrencies());
        assertEquals(3, result.getCurrencies().size());
        assertEquals("United States Dollar", result.getCurrencies().get("USD"));
        assertEquals("Indonesian Rupiah", result.getCurrencies().get("IDR"));
    }

    @Test
    void testFetchData_ErrorHandling() {
        // Arrange
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class))
                .thenReturn(Mono.error(new RuntimeException("Network failure")));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            strategy.fetchData().block();
        });
    }
}

