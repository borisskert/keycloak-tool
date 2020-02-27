package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.TestConfiguration;
import com.github.borisskert.keycloak.config.util.KeycloakAuthentication;
import com.github.borisskert.keycloak.config.util.ResourceLoader;
import com.github.borisskert.keycloak.config.model.KeycloakImport;
import com.github.borisskert.keycloak.config.model.RealmImport;
import com.github.borisskert.keycloak.config.service.KeycloakImportProvider;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import com.github.borisskert.keycloak.config.service.RealmImportService;
import com.github.borisskert.keycloak.config.util.KeycloakRepository;
import com.github.borisskert.keycloak.config.util.SortUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(
        classes = { TestConfiguration.class },
        initializers = { ConfigFileApplicationContextInitializer.class }
)
@ActiveProfiles("IT")
@DirtiesContext
public class ImportRolesIT {
    private static final String REALM_NAME = "realmWithRoles";

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
        File configsFolder = ResourceLoader.loadResource("import-files/roles");
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
        shouldCreateRealmWithRoles();
        shouldAddRealmRole();
        shouldAddClientRole();
        shouldChangeRealmRole();
        shouldChangeClientRole();
        shouldAddUserWithRealmRole();
        shouldAddUserWithClientRole();
        shouldChangeUserAddRealmRole();
        shouldChangeUserAddClientRole();
        shouldChangeUserRemoveRealmRole();
        shouldChangeUserRemoveClientRole();
        shouldAddRealmRoleWithRealmComposite();
        shouldAddRealmRoleWitgClientComposite();
        shouldAddRealmCompositeToRealmRole();
        shouldAddClientCompositeToRealmRole();
        shouldAddCompositeClientToRealmRole();
        shouldAddClientRoleWithRealmRoleComposite();
        shouldAddClientRoleWithClientRoleComposite();
        shouldAddRealmRoleCompositeToClientRole();
        shouldAddClientRoleCompositeToClientRole();
        shouldAddClientRoleCompositesToClientRole();
        shouldRemoveRealmCompositeFromRealmRole();
        shouldRemoveCompositeClientFromRealmRole();
    }

    private void shouldCreateRealmWithRoles() throws Exception {
        doImport("0_create_realm_with_roles.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation createdRealmRole = getRealmRole(
                "my_realm_role"
        );

        assertThat(createdRealmRole.getName(), is("my_realm_role"));
        assertThat(createdRealmRole.isComposite(), is(false));
        assertThat(createdRealmRole.getClientRole(), is(false));
        assertThat(createdRealmRole.getDescription(), is("My realm role"));

        RoleRepresentation createdClientRole = getClientRole(
                "moped-client",
                "my_client_role"
        );

        assertThat(createdClientRole.getName(), is("my_client_role"));
        assertThat(createdClientRole.isComposite(), is(false));
        assertThat(createdClientRole.getClientRole(), is(true));
        assertThat(createdClientRole.getDescription(), is("My moped-client role"));
    }

    private void shouldAddRealmRole() throws Exception {
        doImport("1_update_realm__add_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation createdRealmRole = getRealmRole(
                "my_other_realm_role"
        );

        assertThat(createdRealmRole.getName(), is("my_other_realm_role"));
        assertThat(createdRealmRole.isComposite(), is(false));
        assertThat(createdRealmRole.getClientRole(), is(false));
        assertThat(createdRealmRole.getDescription(), is("My other realm role"));
    }

    private void shouldAddClientRole() throws Exception {
        doImport("2_update_realm__add_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation createdRealmRole = getClientRole(
                "moped-client", "my_other_client_role"
        );

        assertThat(createdRealmRole.getName(), is("my_other_client_role"));
        assertThat(createdRealmRole.isComposite(), is(false));
        assertThat(createdRealmRole.getClientRole(), is(true));
        assertThat(createdRealmRole.getDescription(), is("My other moped-client role"));
    }

    private void shouldChangeRealmRole() throws Exception {
        doImport("3_update_realm__change_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation createdRealmRole = getRealmRole(
                "my_other_realm_role"
        );

        assertThat(createdRealmRole.getName(), is("my_other_realm_role"));
        assertThat(createdRealmRole.isComposite(), is(false));
        assertThat(createdRealmRole.getClientRole(), is(false));
        assertThat(createdRealmRole.getDescription(), is("My changed other realm role"));
    }

    private void shouldChangeClientRole() throws Exception {
        doImport("4_update_realm__change_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation createdRealmRole = getClientRole(
                "moped-client", "my_other_client_role"
        );

        assertThat(createdRealmRole.getName(), is("my_other_client_role"));
        assertThat(createdRealmRole.isComposite(), is(false));
        assertThat(createdRealmRole.getClientRole(), is(true));
        assertThat(createdRealmRole.getDescription(), is("My changed other moped-client role"));
    }

    private void shouldAddUserWithRealmRole() throws Exception {
        doImport("5_update_realm__add_user_with_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        List<String> userRealmLevelRoles = keycloakRepository.getUserRealmLevelRoles(
                REALM_NAME,
                "myuser"
        );

        assertThat(userRealmLevelRoles, hasItem("my_realm_role"));
    }

    private void shouldAddUserWithClientRole() throws Exception {
        doImport("6_update_realm__add_user_with_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        List<String> userClientLevelRoles = keycloakRepository.getUserClientLevelRoles(
                REALM_NAME,
                "myotheruser",
                "moped-client"
        );

        assertThat(userClientLevelRoles, hasItem("my_client_role"));
    }

    private void shouldChangeUserAddRealmRole() throws Exception {
        doImport("7_update_realm__change_user_add_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        List<String> userRealmLevelRoles = keycloakRepository.getUserRealmLevelRoles(
                REALM_NAME,
                "myotheruser"
        );

        assertThat(userRealmLevelRoles, hasItem("my_realm_role"));
    }

    private void shouldChangeUserAddClientRole() throws Exception {
        doImport("8_update_realm__change_user_add_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        List<String> userClientLevelRoles = keycloakRepository.getUserClientLevelRoles(
                REALM_NAME,
                "myuser",
                "moped-client"
        );

        assertThat(userClientLevelRoles, hasItem("my_client_role"));
    }

    private void shouldChangeUserRemoveRealmRole() throws Exception {
        doImport("9_update_realm__change_user_remove_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        List<String> userRealmLevelRoles = keycloakRepository.getUserRealmLevelRoles(
                REALM_NAME,
                "myuser"
        );

        assertThat(userRealmLevelRoles, not(hasItem("my_realm_role")));
    }

    private void shouldChangeUserRemoveClientRole() throws Exception {
        doImport("10_update_realm__change_user_remove_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        List<String> userClientLevelRoles = keycloakRepository.getUserClientLevelRoles(
                REALM_NAME,
                "myotheruser",
                "moped-client"
        );

        assertThat(userClientLevelRoles, not(hasItem("my_client_role")));
    }

    private void shouldAddRealmRoleWithRealmComposite() throws Exception {
        doImport("11_update_realm__add_realm_role_with_realm_composite.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getRealmRole(
                "my_composite_realm_role"
        );

        assertThat(realmRole.getName(), is("my_composite_realm_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(false));
        assertThat(realmRole.getDescription(), is("My added composite realm role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(equalTo(ImmutableSet.of("my_realm_role"))));
        MatcherAssert.assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    private void shouldAddRealmRoleWitgClientComposite() throws Exception {
        doImport("12_update_realm__add_realm_role_with_client_composite.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getRealmRole(
                "my_composite_client_role"
        );

        assertThat(realmRole.getName(), is("my_composite_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(false));
        assertThat(realmRole.getDescription(), is("My added composite client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(nullValue()));
        MatcherAssert.assertThat(composites.getClient(), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role")
        ))));
    }

    private void shouldAddRealmCompositeToRealmRole() throws Exception {
        doImport("13_update_realm__add_realm_composite_to_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getRealmRole(
                "my_composite_realm_role"
        );

        assertThat(realmRole.getName(), is("my_composite_realm_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(false));
        assertThat(realmRole.getDescription(), is("My added composite realm role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(equalTo(ImmutableSet.of("my_realm_role", "my_other_realm_role"))));
        MatcherAssert.assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    private void shouldAddClientCompositeToRealmRole() throws Exception {
        doImport("14_update_realm__add_client_composite_to_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getRealmRole(
                "my_composite_client_role"
        );

        assertThat(realmRole.getName(), is("my_composite_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(false));
        assertThat(realmRole.getDescription(), is("My added composite client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(nullValue()));
        MatcherAssert.assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role")
        ))));
    }

    private void shouldAddCompositeClientToRealmRole() throws Exception {
        doImport("15_update_realm__add_composite_client_to_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getRealmRole(
                "my_composite_client_role"
        );

        assertThat(realmRole.getName(), is("my_composite_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(false));
        assertThat(realmRole.getDescription(), is("My added composite client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(nullValue()));
        MatcherAssert.assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role"),
                "second-moped-client", ImmutableList.of("my_other_second_client_role", "my_second_client_role")
        ))));
    }

    private void shouldAddClientRoleWithRealmRoleComposite() throws Exception {
        doImport("16_update_realm__add_client_role_with_realm_role_composite.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getClientRole(
                "moped-client",
                "my_composite_moped_client_role"
        );

        assertThat(realmRole.getName(), is("my_composite_moped_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(true));
        assertThat(realmRole.getDescription(), is("My composite moped-client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(ImmutableSet.of("my_realm_role")));
        MatcherAssert.assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    private void shouldAddClientRoleWithClientRoleComposite() throws Exception {
        doImport("17_update_realm__add_client_role_with_client_role_composite.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getClientRole(
                "moped-client",
                "my_other_composite_moped_client_role"
        );

        assertThat(realmRole.getName(), is("my_other_composite_moped_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(true));
        assertThat(realmRole.getDescription(), is("My other composite moped-client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(nullValue()));
        MatcherAssert.assertThat(composites.getClient(), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role")
        ))));
    }

    private void shouldAddRealmRoleCompositeToClientRole() throws Exception {
        doImport("18_update_realm__add_realm_role_composite to_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getClientRole(
                "moped-client",
                "my_composite_moped_client_role"
        );

        assertThat(realmRole.getName(), is("my_composite_moped_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(true));
        assertThat(realmRole.getDescription(), is("My composite moped-client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(ImmutableSet.of("my_realm_role", "my_other_realm_role")));
        MatcherAssert.assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    private void shouldAddClientRoleCompositeToClientRole() throws Exception {
        doImport("19_update_realm__add_client_role_composite to_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getClientRole(
                "moped-client",
                "my_other_composite_moped_client_role"
        );

        assertThat(realmRole.getName(), is("my_other_composite_moped_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(true));
        assertThat(realmRole.getDescription(), is("My other composite moped-client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(nullValue()));
        MatcherAssert.assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role")
        ))));
    }

    private void shouldAddClientRoleCompositesToClientRole() throws Exception {
        doImport("20_update_realm__add_client_role_composites_to_client_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getClientRole(
                "moped-client",
                "my_other_composite_moped_client_role"
        );

        assertThat(realmRole.getName(), is("my_other_composite_moped_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(true));
        assertThat(realmRole.getDescription(), is("My other composite moped-client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(nullValue()));
        MatcherAssert.assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role"),
                "second-moped-client", ImmutableList.of("my_other_second_client_role", "my_second_client_role")
        ))));
    }

    private void shouldRemoveRealmCompositeFromRealmRole() throws Exception {
        doImport("21_update_realm__remove_realm_role_composite_from_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getRealmRole(
                "my_composite_realm_role"
        );

        assertThat(realmRole.getName(), is("my_composite_realm_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(false));
        assertThat(realmRole.getDescription(), is("My added composite realm role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(equalTo(ImmutableSet.of("my_other_realm_role"))));
        MatcherAssert.assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    private void shouldRemoveCompositeClientFromRealmRole() throws Exception {
        doImport("22_update_realm__remove_client_role_composite_from_realm_role.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).toRepresentation();

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        RoleRepresentation realmRole = getRealmRole(
                "my_composite_client_role"
        );

        assertThat(realmRole.getName(), is("my_composite_client_role"));
        assertThat(realmRole.isComposite(), is(true));
        assertThat(realmRole.getClientRole(), is(false));
        assertThat(realmRole.getDescription(), is("My added composite client role"));

        RoleRepresentation.Composites composites = realmRole.getComposites();
        MatcherAssert.assertThat(composites, Matchers.is(not(nullValue())));
        MatcherAssert.assertThat(composites.getRealm(), Matchers.is(nullValue()));
        MatcherAssert.assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_other_client_role"),
                "second-moped-client", ImmutableList.of("my_other_second_client_role", "my_second_client_role")
        ))));
    }

    private RoleRepresentation getRealmRole(String roleName) {
        return keycloakProvider.get()
                .realm(REALM_NAME)
                .partialExport(true, true)
                .getRoles()
                .getRealm()
                .stream()
                .filter(r -> Objects.equals(r.getName(), roleName))
                .findFirst()
                .get();
    }

    private RoleRepresentation getClientRole(String clientId, String roleName) {
        return keycloakProvider.get()
                .realm(REALM_NAME)
                .partialExport(true, true)
                .getRoles()
                .getClient()
                .get(clientId)
                .stream()
                .filter(r -> Objects.equals(r.getName(), roleName))
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
