package com.allo.strategy;

import reactor.core.publisher.Mono;

public interface IDRDataFetcher {
    Mono<Object> fetchData();
    String getResourceType();
}

