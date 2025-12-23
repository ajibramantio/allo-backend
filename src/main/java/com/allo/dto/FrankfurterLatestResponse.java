package com.allo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class FrankfurterLatestResponse {
    private String amount;
    private String base;
    private String date;
    private Map<String, Double> rates;
}

