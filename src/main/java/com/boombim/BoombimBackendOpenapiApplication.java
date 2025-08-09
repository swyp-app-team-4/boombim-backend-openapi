package com.boombim;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
@ConfigurationPropertiesScan
public class BoombimBackendOpenapiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BoombimBackendOpenapiApplication.class, args);
    }

}
