package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.TestConfiguration;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import com.github.borisskert.keycloak.config.util.KeycloakImportUtil;
import org.junit.jupiter.api.*;
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
        classes = {TestConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("IT")
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImportSimpleRealmIT {
    private static final String REALM_NAME = "simple";

    @Autowired
    KeycloakProvider keycloakProvider;

    @Autowired
    KeycloakImportUtil importUtil;

    @BeforeEach
    public void setup() throws Exception {
        importUtil.workdir("import-files/simple-realm");
    }

    @AfterEach
    public void cleanup() throws Exception {
        keycloakProvider.close();
    }

    @Test
    @Order(0)
    void shouldCreateSimpleRealm() throws Exception {
        doImport("0_create_simple-realm.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));
        assertThat(createdRealm.getLoginTheme(), is(nullValue()));
        assertThat(
                createdRealm.getAttributes().get("com.github.borisskert.keycloak.config.import-checksum"),
                is("3796660d3087308ee757d9d86e14dd6e6fe4bfd66cc1435851ff2f5c6fa432c5991b3042f95c4f11238e1dfb81676ae2a00bde0bbad17c1f66ef530841df2e66")
        );
    }

    @Test
    @Order(1)
    void shouldNotUpdateSimpleRealm() throws Exception {
        doImport("0.1_update_simple-realm_with_same_config.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));
        assertThat(createdRealm.getLoginTheme(), is(nullValue()));
        assertThat(
                createdRealm.getAttributes().get("com.github.borisskert.keycloak.config.import-checksum"),
                is("3796660d3087308ee757d9d86e14dd6e6fe4bfd66cc1435851ff2f5c6fa432c5991b3042f95c4f11238e1dfb81676ae2a00bde0bbad17c1f66ef530841df2e66")
        );
    }

    @Test
    @Order(2)
    void shouldUpdateSimpleRealm() throws Exception {
        doImport("1_update_login-theme_to_simple-realm.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));
        assertThat(updatedRealm.getLoginTheme(), is("moped"));
        assertThat(
                updatedRealm.getAttributes().get("com.github.borisskert.keycloak.config.import-checksum"),
                is("d3913c179bf6d1ed1afbc2580207f3d7d78efed3ef13f9e12dea3afd5c28e9b307dd930fecfcc100038e540d1e23dc5b5c74d0321a410c7ba330e9dbf9d4211c")
        );
    }

    @Test
    @Order(3)
    void shouldCreateSimpleRealmWithLoginTheme() throws Exception {
        doImport("2_create_simple-realm_with_login-theme.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm("simpleWithLoginTheme").toRepresentation();

        assertThat(createdRealm.getRealm(), is("simpleWithLoginTheme"));
        assertThat(createdRealm.isEnabled(), is(true));
        assertThat(createdRealm.getLoginTheme(), is("moped"));
        assertThat(
                createdRealm.getAttributes().get("com.github.borisskert.keycloak.config.import-checksum"),
                is("5d75698bacb06b1779e2b303069266664d63eec9c52038e2e6ae930bfc6e33ec7e7493b067ee0253e73a6b19cdf8905fd75cc6bb394ca333d32c784063aa65c8")
        );
    }

    private void doImport(String realmImport) {
        importUtil.doImport(realmImport);
    }
}
