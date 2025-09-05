package com.boombim.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kakao")
public record KakaoProperties(
    String baseUrl,
    String scheme,
    String apiKey
) {

}
