package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.KeycloakImportRunnerTestConfiguration;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

@SpringBootTest
@ContextConfiguration(
        classes = {KeycloakImportRunnerTestConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("KeycloakImportRunnerTest")
@DirtiesContext
class KeycloakImportRunnerIT {
    private static final String REALM_NAME = "runner-simple";

    @Autowired
    KeycloakProvider keycloakProvider;

    @Autowired
    KeycloakImportRunner runner;

    @Test
    void shouldImportSimpleRealmDuringRun() throws Exception {
        runner.run();

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));
        assertThat(createdRealm.getLoginTheme(), is(nullValue()));
        assertThat(
                createdRealm.getAttributes().get("com.github.borisskert.keycloak.config.import-checksum"),
                is("00249a06c10598fe9d1db1663cfc8dc6b29b6b91108826669bdcdba628bdf22f6dab719e632e4a6f8f26eb41ea7a14dcdd1c58f9e941db951a509ba11d4624e4")
        );
    }
}
