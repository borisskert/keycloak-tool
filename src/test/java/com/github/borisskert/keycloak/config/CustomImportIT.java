package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.TestConfiguration;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import com.github.borisskert.keycloak.config.util.KeycloakImportUtil;
import com.github.borisskert.keycloak.config.util.KeycloakRepository;
import org.junit.jupiter.api.*;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@SpringBootTest
@ContextConfiguration(
        classes = {TestConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("IT")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class CustomImportIT {
    private static final String REALM_NAME = "realmWithCustomImport";

    @Autowired
    KeycloakProvider keycloakProvider;

    @Autowired
    KeycloakRepository keycloakRepository;

    @Autowired
    KeycloakImportUtil importUtil;

    @BeforeEach
    public void setup() throws Exception {
        importUtil.workdir("import-files/custom-import");
    }

    @AfterEach
    public void cleanup() throws Exception {
        keycloakProvider.close();
    }

    @Test
    @Order(0)
    void shouldCreateRealm() throws Exception {
        doImport("0_create_realm_with_empty_custom-import.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        boolean isImpersonationClientRoleExisting = keycloakRepository.isClientRoleExisting("master", "realmWithCustomImport-realm", "impersonation");

        assertThat(isImpersonationClientRoleExisting, is(true));
    }

    @Test
    @Order(1)
    void shouldRemoveImpersonation() throws Exception {
        doImport("1_update_realm__remove_impersonation.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        boolean isImpersonationClientRoleExisting = keycloakRepository.isClientRoleExisting("master", "realmWithCustomImport-realm", "impersonation");

        assertThat(isImpersonationClientRoleExisting, is(false));
    }

    private void doImport(String realmImport) {
        importUtil.doImport(realmImport);
    }
}
