package com.allo.dto;

import lombok.Data;

import java.util.Map;

@Data
public class FrankfurterCurrenciesResponse {
    private Map<String, String> currencies;
}

