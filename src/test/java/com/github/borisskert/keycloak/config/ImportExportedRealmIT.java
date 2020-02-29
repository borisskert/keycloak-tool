package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.TestConfiguration;
import com.github.borisskert.keycloak.config.model.KeycloakImport;
import com.github.borisskert.keycloak.config.model.RealmImport;
import com.github.borisskert.keycloak.config.service.KeycloakImportProvider;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import com.github.borisskert.keycloak.config.service.RealmImportService;
import com.github.borisskert.keycloak.config.util.ResourceLoader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;

@SpringBootTest
@ContextConfiguration(
        classes = {TestConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("IT")
@DirtiesContext
public class ImportExportedRealmIT {
    private static final Map<String, String> EXPECTED_CHECKSUMS = new HashMap<>();
    private static final String REALM_NAME = "master";

    @Autowired
    RealmImportService realmImportService;

    @Autowired
    KeycloakImportProvider keycloakImportProvider;

    @Autowired
    KeycloakProvider keycloakProvider;

    KeycloakImport keycloakImport;

    String keycloakVersion;

    @BeforeEach
    public void setup() throws Exception {
        keycloakVersion = readKeycloakVersion();
        File configsFolder = ResourceLoader.loadResource("import-files/exported-realm/" + keycloakVersion);
        this.keycloakImport = keycloakImportProvider.readRealmImportsFromDirectory(configsFolder);
    }

    @AfterEach
    public void cleanup() throws Exception {
        keycloakProvider.close();
    }

    @Test
    public void shouldReadImports() {
        assertThat(keycloakImport, is(not(nullValue())));
    }

    @Test
    public void integrationTests() throws Exception {
        shouldImportExportedRealm();
    }

    private void shouldImportExportedRealm() throws Exception {
        doImport("master-realm.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));
        assertThat(updatedRealm.getLoginTheme(), is(nullValue()));
        assertThat(
                updatedRealm.getAttributes().get("com.github.borisskert.keycloak.config.import-checksum"),
                is(expectedImportFileChecksum(keycloakVersion))
        );
    }

    private void doImport(String realmImport) {
        RealmImport foundImport = getImport(realmImport);
        realmImportService.doImport(foundImport);
    }

    private RealmImport getImport(String importName) {
        Map<String, RealmImport> realmImports = keycloakImport.getRealmImports();

        return realmImports.entrySet()
                .stream()
                .filter(e -> e.getKey().equals(importName))
                .map(Map.Entry::getValue)
                .findFirst()
                .get();
    }

    private String readKeycloakVersion() throws IOException {
        Properties properties = new Properties();
        properties.load(readMavenPropertiesFile());

        return properties.getProperty("keycloak.version");
    }

    private InputStream readMavenPropertiesFile() throws FileNotFoundException {
        File targetFile = ResourceLoader.loadTargetFile("maven.properties");
        return new FileInputStream(targetFile);
    }

    private String expectedImportFileChecksum(String keycloakVersion) {
        return EXPECTED_CHECKSUMS.get(keycloakVersion);
    }

    static {
        EXPECTED_CHECKSUMS.put("8.0.2", "bfc8f7708f04c4a9d55a21e9c9dbf42463b4fa96c450b99a7dc0943a8127e6038d7bdfa4b5799fb3bfdbe8eaf5e26ed9fd3fa1bb0a23ea321d7e12488065a931");
        EXPECTED_CHECKSUMS.put("9.0.0", "e150e29a9b5fce1aed40ec330fc5aa04d44b303dc9edc9aa014a44bcaa1c7596c0ec0cf684de7704c8088de87494a262a932ed58da23a997ff497a375d7a337c");
    }
}
