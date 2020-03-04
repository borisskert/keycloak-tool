package com.github.borisskert.keycloak.config.configuration;

import com.github.borisskert.keycloak.config.KeycloakImportRunner;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan(
        basePackages = {"com.github.borisskert.keycloak.config"}
)
@Profile("!KeycloakImportRunnerTest")
public class TestConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @MockBean
    private KeycloakImportRunner keycloakImportRunner;
}
