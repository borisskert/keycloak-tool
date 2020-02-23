package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.TestConfiguration;
import com.github.borisskert.keycloak.config.model.KeycloakImport;
import com.github.borisskert.keycloak.config.model.RealmImport;
import com.github.borisskert.keycloak.config.service.KeycloakImportProvider;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import com.github.borisskert.keycloak.config.service.RealmImportService;
import com.github.borisskert.keycloak.config.util.KeycloakAuthentication;
import com.github.borisskert.keycloak.config.util.KeycloakRepository;
import com.github.borisskert.keycloak.config.util.ResourceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = {TestConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("IT")
@DirtiesContext
public class ImportGroupsIT {
    private static final String REALM_NAME = "realmWithGroups";

    @Autowired
    RealmImportService realmImportService;

    @Autowired
    KeycloakImportProvider keycloakImportProvider;

    @Autowired
    KeycloakProvider keycloakProvider;

    @Autowired
    KeycloakRepository keycloakRepository;

    @Autowired
    KeycloakAuthentication keycloakAuthentication;

    KeycloakImport keycloakImport;

    @Before
    public void setup() throws Exception {
        File configsFolder = ResourceLoader.loadResource("import-files/groups");
        this.keycloakImport = keycloakImportProvider.readRealmImportsFromDirectory(configsFolder);
    }

    @After
    public void cleanup() throws Exception {
        keycloakProvider.close();
    }

    @Test
    public void shouldReadImports() {
        assertThat(keycloakImport, is(not(nullValue())));
    }

    @Test
    public void integrationTests() throws Exception {
        shouldCreateRealmWithGroups();
        shouldUpdateRealmAddGroup();
    }

    private void shouldCreateRealmWithGroups() throws Exception {
        doImport("0_create_realm_with_group.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation createdGroup = loadGroup("/My Group");

        assertThat("name not equal", createdGroup.getName(), is("My Group"));
        assertThat("path not equal", createdGroup.getPath(), is("/My Group"));
        assertThat("attributes not null", createdGroup.getAttributes(), is(nullValue()));
        assertThat("realm roles not null", createdGroup.getRealmRoles(), is(nullValue()));
        assertThat("client roles not null", createdGroup.getClientRoles(), is(nullValue()));
        assertThat("subgroups not empty", createdGroup.getSubGroups(), is(equalTo(new ArrayList<>())));
    }

    private void shouldUpdateRealmAddGroup() throws Exception {
        doImport("1_update_realm_add_group.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation existingGroup = loadGroup("/My Group");

        assertThat("name not equal", existingGroup.getName(), is("My Group"));
        assertThat("path not equal", existingGroup.getPath(), is("/My Group"));
        assertThat("attributes not null", existingGroup.getAttributes(), is(nullValue()));
        assertThat("realm roles not null", existingGroup.getRealmRoles(), is(nullValue()));
        assertThat("client roles not null", existingGroup.getClientRoles(), is(nullValue()));
        assertThat("subgroups not empty", existingGroup.getSubGroups(), is(equalTo(new ArrayList<>())));

        GroupRepresentation addedGroup = loadGroup("/My Added Group");

        assertThat("name not equal", addedGroup.getName(), is("My Added Group"));
        assertThat("path not equal", addedGroup.getPath(), is("/My Added Group"));
        assertThat("attributes not null", addedGroup.getAttributes(), is(nullValue()));
        assertThat("realm roles not null", addedGroup.getRealmRoles(), is(nullValue()));
        assertThat("client roles not null", addedGroup.getClientRoles(), is(nullValue()));
        assertThat("subgroups not empty", addedGroup.getSubGroups(), is(equalTo(new ArrayList<>())));
    }

    private GroupRepresentation loadGroup(String groupPath) {
        return keycloakProvider.get()
                .realm(REALM_NAME)
                .groups()
                .groups()
                .stream()
                .filter(g -> Objects.equals(groupPath, g.getPath()))
                .findFirst()
                .get();
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
}
