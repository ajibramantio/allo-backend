package com.allo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class FrankfurterHistoricalResponse {
    private String amount;
    private String base;
    private String start_date;
    private String end_date;
    private Map<String, Map<String, Double>> rates;
}

