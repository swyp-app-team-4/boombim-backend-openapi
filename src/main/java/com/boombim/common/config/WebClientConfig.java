package com.boombim.common.config;

import static com.boombim.common.constant.OpenApiConstant.*;
import static org.springframework.http.HttpHeaders.*;

import com.boombim.common.properties.KakaoProperties;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient openApiWebClient() {
        return WebClient.builder()
            .baseUrl(BASE_URL)
            .defaultHeader(CONTENT_TYPE_HEADER, CONTENT_TYPE_JSON)
            .build();
    }

    @Bean
    public WebClient kakaoWebClient(
        KakaoProperties properties
    ) {
        HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(3));

        return WebClient.builder()
            .baseUrl(properties.baseUrl())
            .defaultHeader(AUTHORIZATION, properties.scheme() + properties.apiKey())
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
