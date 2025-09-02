package com.boombim.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "s3")
public record S3Properties(
    String bucketName,
    String region,
    String accessKey,
    String secretKey,
    String baseUrl,
    String placeholderKey
) {

}
