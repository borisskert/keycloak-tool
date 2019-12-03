package com.github.borisskert.keycloak.config.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(basePackages = { "com.github.borisskert.keycloak.config" })
public class TestConfiguration {

    @Bean
    @Primary
    @Qualifier("json")
    public ObjectMapper createJsonObjectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
