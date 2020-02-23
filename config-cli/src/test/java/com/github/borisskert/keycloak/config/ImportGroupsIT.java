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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.resource.GroupsResource;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
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
        shouldUpdateRealmAddGroupWithAttribute();
        shouldUpdateRealmAddGroupWithRealmRole();
        shouldUpdateRealmAddGroupWithClientRole();
        shouldUpdateRealmAddGroupWithSubgroup();
        shouldUpdateRealmAddGroupWithSubgroupWithRealmRole();
    }

    private void shouldCreateRealmWithGroups() throws Exception {
        doImport("0_create_realm_with_group.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation createdGroup = loadGroup("/My Group");

        assertThat("name not equal", createdGroup.getName(), is("My Group"));
        assertThat("path not equal", createdGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", createdGroup.getAttributes(), is(equalTo(new HashMap<>())));
        assertThat("realm roles is null", createdGroup.getRealmRoles(), is(equalTo(new ArrayList<>())));
        assertThat("client roles not null", createdGroup.getClientRoles(), is(equalTo(new HashMap<>())));
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
        assertThat("attributes is null", existingGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", existingGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", existingGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups is null", existingGroup.getSubGroups(), is(equalTo(ImmutableList.of())));

        GroupRepresentation addedGroup = loadGroup("/My Added Group");

        assertThat("name not equal", addedGroup.getName(), is("My Added Group"));
        assertThat("path not equal", addedGroup.getPath(), is("/My Added Group"));
        assertThat("attributes is null", addedGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", addedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", addedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups is null", addedGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmAddGroupWithAttribute() throws Exception {
        doImport("2_update_realm_add_group_with_attribute.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation addedGroup = loadGroup("/Group with attribute");

        assertThat("name not equal", addedGroup.getName(), is("Group with attribute"));
        assertThat("path not equal", addedGroup.getPath(), is("/Group with attribute"));
        assertThat("attributes is null", addedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of("my attribute", ImmutableList.of("my attribute value"))
        )));
        assertThat("realm roles is null", addedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", addedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups is null", addedGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmAddGroupWithRealmRole() throws Exception {
        doImport("3_update_realm_add_group_with_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation addedGroup = loadGroup("/Group with realm role");

        assertThat("name not equal", addedGroup.getName(), is("Group with realm role"));
        assertThat("path not equal", addedGroup.getPath(), is("/Group with realm role"));
        assertThat("attributes is null", addedGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", addedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", addedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups is null", addedGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmAddGroupWithClientRole() throws Exception {
        doImport("4_update_realm_add_group_with_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation addedGroup = loadGroup("/Group with client role");

        assertThat("name not equal", addedGroup.getName(), is("Group with client role"));
        assertThat("path not equal", addedGroup.getPath(), is("/Group with client role"));
        assertThat("attributes is null", addedGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", addedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", addedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));
        assertThat("subgroups is null", addedGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmAddGroupWithSubgroup() throws Exception {
        doImport("5_update_realm_add_group_with_subgroup.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation addedGroup = loadGroup("/Group with subgroup");

        assertThat("name not equal", addedGroup.getName(), is("Group with subgroup"));
        assertThat("path not equal", addedGroup.getPath(), is("/Group with subgroup"));
        assertThat("attributes is null", addedGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", addedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", addedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups is null", addedGroup.getSubGroups(), is(not(nullValue())));
        assertThat("subgroups is empty", addedGroup.getSubGroups(), is(hasSize(1)));

        GroupRepresentation subgroup = addedGroup.getSubGroups().get(0);
        assertThat("subgroup is null", subgroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subgroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subgroup.getPath(), is("/Group with subgroup/My SubGroup"));
        assertThat("subgroup's attributes is null", subgroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subgroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subgroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subgroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmAddGroupWithSubgroupWithRealmRole() throws Exception {
        doImport("6_update_realm_add_group_with_subgroup.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation addedGroup = loadGroup("/Group with subgroup with realm role");

        assertThat("name not equal", addedGroup.getName(), is("Group with subgroup with realm role"));
        assertThat("path not equal", addedGroup.getPath(), is("/Group with subgroup with realm role"));
        assertThat("attributes is null", addedGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", addedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", addedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups is null", addedGroup.getSubGroups(), is(not(nullValue())));
        assertThat("subgroups is empty", addedGroup.getSubGroups(), is(hasSize(1)));

        GroupRepresentation subgroup = addedGroup.getSubGroups().get(0);
        assertThat("subgroup is null", subgroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subgroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subgroup.getPath(), is("/Group with subgroup with realm role/My SubGroup"));
        assertThat("subgroup's attributes is null", subgroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subgroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_second_realm_role"))));
        assertThat("subgroup's client roles is null", subgroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subgroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private GroupRepresentation loadGroup(String groupPath) {
        GroupsResource groupsResource = keycloakProvider.get()
                .realm(REALM_NAME)
                .groups();

        GroupRepresentation groupRepresentation = groupsResource
                .groups()
                .stream()
                .filter(g -> Objects.equals(groupPath, g.getPath()))
                .findFirst()
                .get();

        return groupsResource
                .group(groupRepresentation.getId())
                .toRepresentation();
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
