package com.allo.config;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientFactoryBean implements FactoryBean<WebClient> {

    private final FrankfurterApiProperties properties;

    @Autowired
    public WebClientFactoryBean(@Lazy FrankfurterApiProperties properties) {
        this.properties = properties;
    }

    @Override
    public WebClient getObject() throws Exception {
        return WebClient.builder()
                .baseUrl(properties.getBaseUrl())
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();
    }

    @Override
    public Class<?> getObjectType() {
        return WebClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}

