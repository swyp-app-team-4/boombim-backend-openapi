package com.boombim.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "official-congestion.purge")
public record OfficialCongestionPurgeProperties(
    boolean enabled,
    int days,
    int batchSize
) {

}
