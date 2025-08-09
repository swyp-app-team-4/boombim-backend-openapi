package com.boombim.common.config;

import static com.boombim.common.constant.OpenApiConstant.*;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient openApiWebClient() {
        return WebClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
            .build();
    }

}
