package com.boombim.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "open-api")
public record OpenApiProperties(
    String apiKey
) {

}
