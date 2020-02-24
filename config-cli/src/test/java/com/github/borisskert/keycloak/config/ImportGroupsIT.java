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
import java.util.*;

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
        shouldUpdateRealmAddGroupWithSubGroup();
        shouldUpdateRealmAddGroupWithSubGroupWithRealmRole();
        shouldUpdateRealmAddGroupWithSubGroupWithClientRole();
        shouldUpdateRealmAddGroupWithSubGroupWithSubGroup();
        shouldUpdateRealmUpdateGroupAddAttribute();
        shouldUpdateRealmUpdateGroupAddRealmRole();
        shouldUpdateRealmUpdateGroupAddClientRole();
        shouldUpdateRealmUpdateGroupAddSubgroup();
        shouldUpdateRealmUpdateGroupAddSecondAttributeValue();
        shouldUpdateRealmUpdateGroupAddSecondAttribute();
        shouldUpdateRealmUpdateGroupChangeAttributeValue();
        shouldUpdateRealmUpdateGroupChangeAttributeKey();
        shouldUpdateRealmUpdateGroupDeleteAttribute();
        shouldUpdateRealmUpdateGroupDeleteAttributeValue();
        shouldUpdateRealmUpdateGroupAddSecondRealmRole();
        shouldUpdateRealmUpdateGroupDeleteRealmRole();
        shouldUpdateRealmUpdateGroupDeleteLastRealmRole();
        shouldUpdateRealmUpdateGroupAddSecondClientRole();
        shouldUpdateRealmUpdateGroupRemoveClientRole();
        shouldUpdateRealmUpdateGroupAddClientRolesFromSecondClient();
        shouldUpdateRealmUpdateGroupRemoveClient();
    }

    private void shouldCreateRealmWithGroups() throws Exception {
        doImport("0_create_realm_with_group.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation createdGroup = loadGroup("/My Group");

        assertThat("name not equal", createdGroup.getName(), is("My Group"));
        assertThat("path not equal", createdGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", createdGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", createdGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles not null", createdGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups not empty", createdGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
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

    private void shouldUpdateRealmAddGroupWithSubGroup() throws Exception {
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

        GroupRepresentation subGroup = addedGroup.getSubGroups().get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/Group with subgroup/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmAddGroupWithSubGroupWithRealmRole() throws Exception {
        doImport("6_update_realm_add_group_with_subgroup_with_realm_role.json");

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

        GroupRepresentation subGroup = addedGroup.getSubGroups().get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/Group with subgroup with realm role/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_second_realm_role"))));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmAddGroupWithSubGroupWithClientRole() throws Exception {
        doImport("7_update_realm_add_group_with_subgroup_with_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation addedGroup = loadGroup("/Group with subgroup with client role");

        assertThat("name not equal", addedGroup.getName(), is("Group with subgroup with client role"));
        assertThat("path not equal", addedGroup.getPath(), is("/Group with subgroup with client role"));
        assertThat("attributes is null", addedGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", addedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", addedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups is null", addedGroup.getSubGroups(), is(not(nullValue())));
        assertThat("subgroups is empty", addedGroup.getSubGroups(), is(hasSize(1)));

        GroupRepresentation subGroup = addedGroup.getSubGroups().get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/Group with subgroup with client role/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_second_client_role")))));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmAddGroupWithSubGroupWithSubGroup() throws Exception {
        doImport("8_update_realm_add_group_with_subgroup_with_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation addedGroup = loadGroup("/Group with subgroup with subgroup");

        assertThat("name not equal", addedGroup.getName(), is("Group with subgroup with subgroup"));
        assertThat("path not equal", addedGroup.getPath(), is("/Group with subgroup with subgroup"));
        assertThat("attributes is null", addedGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("realm roles is null", addedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", addedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        List<GroupRepresentation> subGroups = addedGroup.getSubGroups();
        assertThat("subgroups is null", subGroups, is(not(nullValue())));
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/Group with subgroup with subgroup/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));

        List<GroupRepresentation> innerSubGroups = subGroup.getSubGroups();
        assertThat("subgroup's subgroups is null", innerSubGroups, is(hasSize(1)));
        GroupRepresentation innerSubGroup = innerSubGroups.get(0);
        assertThat("subgroup is null", innerSubGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", innerSubGroup.getName(), is("My Inner SubGroup"));
        assertThat("subgroup's path not equal", innerSubGroup.getPath(), is("/Group with subgroup with subgroup/My SubGroup/My Inner SubGroup"));
        assertThat("subgroup's attributes is null", innerSubGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", innerSubGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", innerSubGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddAttribute() throws Exception {
        doImport("9_update_realm_update_group_add_attribute.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of("my added attribute", ImmutableList.of("my added attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups not empty", updatedGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddRealmRole() throws Exception {
        doImport("10_update_realm_update_group_add_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of("my added attribute", ImmutableList.of("my added attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroups not empty", updatedGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddClientRole() throws Exception {
        doImport("11_update_realm_update_group_add_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of("my added attribute", ImmutableList.of("my added attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));
        assertThat("subgroups not empty", updatedGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddSubgroup() throws Exception {
        doImport("12_update_realm_update_group_add_subgroup.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of("my added attribute", ImmutableList.of("my added attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddSecondAttributeValue() throws Exception {
        doImport("13_update_realm_update_group_add_second_attribute_value.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of("my added attribute", ImmutableList.of("my added attribute value", "my added attribute second value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddSecondAttribute() throws Exception {
        doImport("14_update_realm_update_group_add_second_attribute.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my added attribute", ImmutableList.of("my added attribute value", "my added attribute second value"),
                        "my second added attribute", ImmutableList.of("my second added attribute value", "my second added attribute second value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupChangeAttributeValue() throws Exception {
        doImport("15_update_realm_update_group_change_attribute_value.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my added attribute", ImmutableList.of("my added attribute value", "my added attribute second value"),
                        "my second added attribute", ImmutableList.of("my changed attribute value", "my second added attribute second value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupChangeAttributeKey() throws Exception {
        doImport("16_update_realm_update_group_change_attribute_key.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my added attribute", ImmutableList.of("my added attribute value", "my added attribute second value"),
                        "my changed attribute", ImmutableList.of("my changed attribute value", "my second added attribute second value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupDeleteAttribute() throws Exception {
        doImport("17_update_realm_update_group_delete_attribute.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value", "my second added attribute second value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupDeleteAttributeValue() throws Exception {
        doImport("18_update_realm_update_group_delete_attribute_value.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddSecondRealmRole() throws Exception {
        doImport("19_update_realm_update_group_add_scond_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value"))
        )));
        assertThat("realm roles is null", sorted(updatedGroup.getRealmRoles()), is(equalTo(ImmutableList.of("my_realm_role", "my_second_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupDeleteRealmRole() throws Exception {
        doImport("20_update_realm_update_group_delete_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of("my_second_realm_role"))));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupDeleteLastRealmRole() throws Exception {
        doImport("21_update_realm_update_group_delete_last_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(ImmutableMap.of("moped-client", ImmutableList.of("my_client_role")))));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddSecondClientRole() throws Exception {
        doImport("22_update_realm_update_group_delete_add_second_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", sorted(updatedGroup.getClientRoles()), is(equalTo(
                ImmutableMap.of("moped-client", ImmutableList.of("my_client_role", "my_second_client_role"))
        )));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupRemoveClientRole() throws Exception {
        doImport("23_update_realm_update_group_delete_remove_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", updatedGroup.getClientRoles(), is(equalTo(
                ImmutableMap.of("moped-client", ImmutableList.of("my_second_client_role"))
        )));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupAddClientRolesFromSecondClient() throws Exception {
        doImport("24_update_realm_update_group_delete_add_client_roles_from_second_client.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", sorted(updatedGroup.getClientRoles()), is(equalTo(
                ImmutableMap.of(
                        "moped-client", ImmutableList.of("my_second_client_role"),
                        "second-moped-client", ImmutableList.of(
                                "my_client_role_of_second-moped-client",
                                "my_second_client_role_of_second-moped-client"
                        ))
        )));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
    }

    private void shouldUpdateRealmUpdateGroupRemoveClient() throws Exception {
        doImport("25_update_realm_update_group_delete_remove_client.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        GroupRepresentation updatedGroup = loadGroup("/My Group");

        assertThat("name not equal", updatedGroup.getName(), is("My Group"));
        assertThat("path not equal", updatedGroup.getPath(), is("/My Group"));
        assertThat("attributes is null", updatedGroup.getAttributes(), is(equalTo(
                ImmutableMap.of(
                        "my changed attribute", ImmutableList.of("my changed attribute value"))
        )));
        assertThat("realm roles is null", updatedGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("client roles is null", sorted(updatedGroup.getClientRoles()), is(equalTo(
                ImmutableMap.of(
                        "second-moped-client", ImmutableList.of(
                                "my_client_role_of_second-moped-client",
                                "my_second_client_role_of_second-moped-client"
                        ))
        )));

        List<GroupRepresentation> subGroups = updatedGroup.getSubGroups();
        assertThat("subgroups is empty", subGroups, is(hasSize(1)));

        GroupRepresentation subGroup = subGroups.get(0);
        assertThat("subgroup is null", subGroup, is(not(nullValue())));
        assertThat("subgroup's name not equal", subGroup.getName(), is("My SubGroup"));
        assertThat("subgroup's path not equal", subGroup.getPath(), is("/My Group/My SubGroup"));
        assertThat("subgroup's attributes is null", subGroup.getAttributes(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's realm roles is null", subGroup.getRealmRoles(), is(equalTo(ImmutableList.of())));
        assertThat("subgroup's client roles is null", subGroup.getClientRoles(), is(equalTo(ImmutableMap.of())));
        assertThat("subgroup's subgroups is null", subGroup.getSubGroups(), is(equalTo(ImmutableList.of())));
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

    private <T> List<T> sorted(List<T> unsorted) {
        ArrayList<T> toSort = new ArrayList<>(unsorted);
        Collections.sort((List)toSort);

        return toSort;
    }

    private <T> Map<String, List<T>> sorted(Map<String, List<T>> unsorted) {
        Map<String, List<T>> toSort = new HashMap<>(unsorted);

        for (List<T> value : toSort.values()) {
            Collections.sort((List)value);
        }

        return toSort;
    }
}
