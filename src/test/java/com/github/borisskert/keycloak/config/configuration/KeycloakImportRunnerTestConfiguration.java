package com.github.borisskert.keycloak.config.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(
        basePackages = {"com.github.borisskert.keycloak.config"}
)
@Profile("!IT")
public class KeycloakImportRunnerTestConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
