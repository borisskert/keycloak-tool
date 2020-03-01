package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.TestConfiguration;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import com.github.borisskert.keycloak.config.util.KeycloakImportUtil;
import com.github.borisskert.keycloak.config.util.KeycloakRepository;
import com.github.borisskert.keycloak.config.util.SortUtils;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
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
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImportRolesIT {
    private static final String REALM_NAME = "realmWithRoles";

    @Autowired
    KeycloakProvider keycloakProvider;

    @Autowired
    KeycloakRepository keycloakRepository;

    @Autowired
    KeycloakImportUtil importUtil;

    @BeforeEach
    void setup() throws Exception {
        importUtil.workdir("import-files/roles");
    }

    @AfterEach
    void cleanup() throws Exception {
        keycloakProvider.close();
    }

    @Test
    @Order(0)
    void shouldCreateRealmWithRoles() throws Exception {
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

    @Test
    @Order(1)
    void shouldAddRealmRole() throws Exception {
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

    @Test
    @Order(2)
    void shouldAddClientRole() throws Exception {
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

    @Test
    @Order(3)
    void shouldChangeRealmRole() throws Exception {
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

    @Test
    @Order(4)
    void shouldChangeClientRole() throws Exception {
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

    @Test
    @Order(5)
    void shouldAddUserWithRealmRole() throws Exception {
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

    @Test
    @Order(6)
    void shouldAddUserWithClientRole() throws Exception {
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

    @Test
    @Order(7)
    void shouldChangeUserAddRealmRole() throws Exception {
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

    @Test
    @Order(8)
    void shouldChangeUserAddClientRole() throws Exception {
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

    @Test
    @Order(9)
    void shouldChangeUserRemoveRealmRole() throws Exception {
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

    @Test
    @Order(10)
    void shouldChangeUserRemoveClientRole() throws Exception {
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

    @Test
    @Order(11)
    void shouldAddRealmRoleWithRealmComposite() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(equalTo(ImmutableSet.of("my_realm_role"))));
        assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    @Test
    @Order(12)
    void shouldAddRealmRoleWitgClientComposite() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(nullValue()));
        assertThat(composites.getClient(), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role")
        ))));
    }

    @Test
    @Order(13)
    void shouldAddRealmCompositeToRealmRole() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(equalTo(ImmutableSet.of("my_realm_role", "my_other_realm_role"))));
        assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    @Test
    @Order(14)
    void shouldAddClientCompositeToRealmRole() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(nullValue()));
        assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role")
        ))));
    }

    @Test
    @Order(15)
    void shouldAddCompositeClientToRealmRole() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(nullValue()));
        assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role"),
                "second-moped-client", ImmutableList.of("my_other_second_client_role", "my_second_client_role")
        ))));
    }

    @Test
    @Order(16)
    void shouldAddClientRoleWithRealmRoleComposite() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(ImmutableSet.of("my_realm_role")));
        assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    @Test
    @Order(17)
    void shouldAddClientRoleWithClientRoleComposite() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(nullValue()));
        assertThat(composites.getClient(), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role")
        ))));
    }

    @Test
    @Order(18)
    void shouldAddRealmRoleCompositeToClientRole() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(ImmutableSet.of("my_realm_role", "my_other_realm_role")));
        assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    @Test
    @Order(19)
    void shouldAddClientRoleCompositeToClientRole() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(nullValue()));
        assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role")
        ))));
    }

    @Test
    @Order(20)
    void shouldAddClientRoleCompositesToClientRole() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(nullValue()));
        assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role"),
                "second-moped-client", ImmutableList.of("my_other_second_client_role", "my_second_client_role")
        ))));
    }

    @Test
    @Order(21)
    void shouldRemoveRealmCompositeFromRealmRole() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(equalTo(ImmutableSet.of("my_other_realm_role"))));
        assertThat(composites.getClient(), Matchers.is(nullValue()));
    }

    @Test
    @Order(22)
    void shouldRemoveCompositeClientFromRealmRole() throws Exception {
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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(nullValue()));
        assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_other_client_role"),
                "second-moped-client", ImmutableList.of("my_other_second_client_role", "my_second_client_role")
        ))));
    }

    @Test
    @Order(23)
    void shouldRemoveClientCompositesFromRealmRole() throws Exception {
        doImport("23_update_realm__remove_client_role_composites_from_realm_role.json");

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
        assertThat(composites, Matchers.is(not(nullValue())));
        assertThat(composites.getRealm(), Matchers.is(nullValue()));
        assertThat(composites.getClient(), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_other_client_role")
        ))));
    }

    @Test
    @Order(24)
    void shouldRemoveRealmCompositeFromClientRole() throws Exception {
        doImport("24_update_realm__remove_realm_role_composite_from_client_role.json");

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
        assertThat(composites, is(not(nullValue())));
        assertThat(composites.getRealm(), is(ImmutableSet.of("my_other_realm_role")));
        assertThat(composites.getClient(), is(nullValue()));
    }

    @Test
    @Order(25)
    void shouldRemoveClientCompositeFromClientRole() throws Exception {
        doImport("25_update_realm__remove_client_role_composite_from_client_role.json");

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
        assertThat(composites, is(not(nullValue())));
        assertThat(composites.getRealm(), is(nullValue()));
        assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "moped-client", ImmutableList.of("my_client_role", "my_other_client_role"),
                "second-moped-client", ImmutableList.of("my_other_second_client_role")
        ))));
    }

    @Test
    @Order(26)
    void shouldRemoveClientCompositesFromClientRole() throws Exception {
        doImport("26_update_realm__remove_client_role_composites_from_client_role.json");

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
        assertThat(composites, is(not(nullValue())));
        assertThat(composites.getRealm(), is(nullValue()));
        assertThat(SortUtils.sorted(composites.getClient()), Matchers.is(equalTo(ImmutableMap.of(
                "second-moped-client", ImmutableList.of("my_other_second_client_role")
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
        importUtil.doImport(realmImport);
    }
}
