package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.TestConfiguration;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import com.github.borisskert.keycloak.config.util.KeycloakImportUtil;
import com.github.borisskert.keycloak.config.util.KeycloakRepository;
import org.junit.jupiter.api.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;

@SpringBootTest
@ContextConfiguration(
        classes = {TestConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("IT")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImportClientsIT {
    private static final String REALM_NAME = "realmWithClients";

    @Autowired
    KeycloakProvider keycloakProvider;

    @Autowired
    KeycloakRepository keycloakRepository;

    @Autowired
    KeycloakImportUtil importUtil;

    @BeforeEach
    public void setup() throws Exception {
        importUtil.workdir("import-files/clients");
    }

    @AfterEach
    public void cleanup() throws Exception {
        keycloakProvider.close();
    }

    @Test
    @Order(0)
    void shouldCreateRealmWithClient() throws Exception {
        doImport("0_create_realm_with_client.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        ClientRepresentation createdClient = keycloakRepository.getClient(
                REALM_NAME,
                "moped-client"
        );

        assertThat(createdClient.getName(), is("moped-client"));
        assertThat(createdClient.getClientId(), is("moped-client"));
        assertThat(createdClient.getDescription(), is("Moped-Client"));
        assertThat(createdClient.isEnabled(), is(true));
        assertThat(createdClient.getClientAuthenticatorType(), is("client-secret"));
        assertThat(createdClient.getRedirectUris(), is(containsInAnyOrder("*")));
        assertThat(createdClient.getWebOrigins(), is(containsInAnyOrder("*")));

        // client secret on this place is always null...
        assertThat(createdClient.getSecret(), is(nullValue()));

        // ... and has to be retrieved separately
        String clientSecret = getClientSecret(createdClient.getId());
        assertThat(clientSecret, is("my-special-client-secret"));
    }

    @Test
    @Order(1)
    void shouldUpdateRealmByAddingClient() throws Exception {
        doImport("1_update_realm__add_client.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        ClientRepresentation createdClient = keycloakRepository.getClient(
                REALM_NAME,
                "another-client"
        );

        assertThat(createdClient.getName(), is("another-client"));
        assertThat(createdClient.getClientId(), is("another-client"));
        assertThat(createdClient.getDescription(), is("Another-Client"));
        assertThat(createdClient.isEnabled(), is(true));
        assertThat(createdClient.getClientAuthenticatorType(), is("client-secret"));
        assertThat(createdClient.getRedirectUris(), is(containsInAnyOrder("*")));
        assertThat(createdClient.getWebOrigins(), is(containsInAnyOrder("*")));

        // client secret on this place is always null...
        assertThat(createdClient.getSecret(), is(nullValue()));

        // ... and has to be retrieved separately
        String clientSecret = getClientSecret(createdClient.getId());
        assertThat(clientSecret, is("my-other-client-secret"));
    }

    @Test
    @Order(2)
    void shouldUpdateRealmWithChangedClientProperties() throws Exception {
        doImport("2_update_realm__change_clients_properties.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        ClientRepresentation createdClient = keycloakRepository.getClient(
                REALM_NAME,
                "moped-client"
        );

        assertThat(createdClient.getName(), is("moped-client"));
        assertThat(createdClient.getClientId(), is("moped-client"));
        assertThat(createdClient.getDescription(), is("Moped-Client"));
        assertThat(createdClient.isEnabled(), is(true));
        assertThat(createdClient.getClientAuthenticatorType(), is("client-secret"));
        assertThat(createdClient.getRedirectUris(), is(containsInAnyOrder("https://moped-client.org/redirect")));
        assertThat(createdClient.getWebOrigins(), is(containsInAnyOrder("https://moped-client.org/webOrigin")));

        // client secret on this place is always null...
        assertThat(createdClient.getSecret(), is(nullValue()));

        // ... and has to be retrieved separately
        String clientSecret = getClientSecret(createdClient.getId());
        assertThat(clientSecret, is("changed-special-client-secret"));
    }

    /**
     * @param id (not client-id)
     */
    private String getClientSecret(String id) {
        CredentialRepresentation secret = keycloakProvider.get()
                .realm(REALM_NAME)
                .clients().get(id).getSecret();

        return secret.getValue();
    }

    private void doImport(String realmImport) {
        importUtil.doImport(realmImport);
    }
}
