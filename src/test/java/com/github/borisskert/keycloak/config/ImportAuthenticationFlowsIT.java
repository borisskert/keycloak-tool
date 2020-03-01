package com.github.borisskert.keycloak.config;

import com.github.borisskert.keycloak.config.configuration.TestConfiguration;
import com.github.borisskert.keycloak.config.exception.ImportProcessingException;
import com.github.borisskert.keycloak.config.exception.InvalidImportException;
import com.github.borisskert.keycloak.config.model.RealmImport;
import com.github.borisskert.keycloak.config.service.KeycloakProvider;
import com.github.borisskert.keycloak.config.service.RealmImportService;
import com.github.borisskert.keycloak.config.util.KeycloakImportUtil;
import com.googlecode.catchexception.apis.CatchExceptionHamcrestMatchers;
import org.junit.jupiter.api.*;
import org.keycloak.representations.idm.AuthenticationExecutionExportRepresentation;
import org.keycloak.representations.idm.AuthenticationFlowRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;
import java.util.Optional;

import static com.googlecode.catchexception.CatchException.catchException;
import static com.googlecode.catchexception.CatchException.caughtException;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsInstanceOf.instanceOf;

@SpringBootTest
@ContextConfiguration(
        classes = {TestConfiguration.class},
        initializers = {ConfigFileApplicationContextInitializer.class}
)
@ActiveProfiles("IT")
@DirtiesContext
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ImportAuthenticationFlowsIT {
    private static final String REALM_NAME = "realmWithFlow";

    @Autowired
    RealmImportService realmImportService;

    @Autowired
    KeycloakProvider keycloakProvider;

    @Autowired
    KeycloakImportUtil importUtil;

    @BeforeEach
    public void setup() throws Exception {
        importUtil.workdir("import-files/auth-flows");
    }

    @AfterEach
    public void cleanup() throws Exception {
        keycloakProvider.close();
    }

    @Test
    @Order(0)
    void shouldCreateRealmWithFlows() throws Exception {
        doImport("0_create_realm_with_flows.json");

        RealmRepresentation createdRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(createdRealm.getRealm(), is(REALM_NAME));
        assertThat(createdRealm.isEnabled(), is(true));

        AuthenticationFlowRepresentation importedFlow = getAuthenticationFlow(createdRealm, "my auth flow");
        assertThat(importedFlow.getDescription(), is("My auth flow for testing"));
        assertThat(importedFlow.getProviderId(), is("basic-flow"));
        assertThat(importedFlow.isBuiltIn(), is(false));
        assertThat(importedFlow.isTopLevel(), is(true));

        List<AuthenticationExecutionExportRepresentation> importedExecutions = importedFlow.getAuthenticationExecutions();
        assertThat(importedExecutions, hasSize(1));

        AuthenticationExecutionExportRepresentation importedExecution = getExecutionFromFlow(importedFlow, "docker-http-basic-authenticator");
        assertThat(importedExecution.getAuthenticator(), is("docker-http-basic-authenticator"));
        assertThat(importedExecution.getRequirement(), is("DISABLED"));
        assertThat(importedExecution.getPriority(), is(0));
        assertThat(importedExecution.isAutheticatorFlow(), is(false));
    }

    @Test
    @Order(1)
    void shouldAddExecutionToFlow() {
        doImport("1_update_realm__add_execution_to_flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        AuthenticationFlowRepresentation unchangedFlow = getAuthenticationFlow(updatedRealm, "my auth flow");
        assertThat(unchangedFlow.getDescription(), is("My auth flow for testing"));
        assertThat(unchangedFlow.getProviderId(), is("basic-flow"));
        assertThat(unchangedFlow.isBuiltIn(), is(false));
        assertThat(unchangedFlow.isTopLevel(), is(true));

        List<AuthenticationExecutionExportRepresentation> importedExecutions = unchangedFlow.getAuthenticationExecutions();
        assertThat(importedExecutions, hasSize(2));

        AuthenticationExecutionExportRepresentation importedExecution = getExecutionFromFlow(unchangedFlow, "docker-http-basic-authenticator");
        assertThat(importedExecution.getAuthenticator(), is("docker-http-basic-authenticator"));
        assertThat(importedExecution.getRequirement(), is("DISABLED"));
        assertThat(importedExecution.getPriority(), is(0));
        assertThat(importedExecution.isAutheticatorFlow(), is(false));

        importedExecution = getExecutionFromFlow(unchangedFlow, "http-basic-authenticator");
        assertThat(importedExecution.getAuthenticator(), is("http-basic-authenticator"));
        assertThat(importedExecution.getRequirement(), is("DISABLED"));
        assertThat(importedExecution.getPriority(), is(1));
        assertThat(importedExecution.isAutheticatorFlow(), is(false));
    }

    @Test
    @Order(2)
    void shouldChangeExecutionRequirement() {
        doImport("2_update_realm__change_execution_requirement.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        AuthenticationFlowRepresentation unchangedFlow = getAuthenticationFlow(updatedRealm, "my auth flow");
        assertThat(unchangedFlow.getDescription(), is("My auth flow for testing"));
        assertThat(unchangedFlow.getProviderId(), is("basic-flow"));
        assertThat(unchangedFlow.isBuiltIn(), is(false));
        assertThat(unchangedFlow.isTopLevel(), is(true));

        List<AuthenticationExecutionExportRepresentation> importedExecutions = unchangedFlow.getAuthenticationExecutions();
        assertThat(importedExecutions, hasSize(2));

        AuthenticationExecutionExportRepresentation importedExecution = getExecutionFromFlow(unchangedFlow, "docker-http-basic-authenticator");
        assertThat(importedExecution.getAuthenticator(), is("docker-http-basic-authenticator"));
        assertThat(importedExecution.getRequirement(), is("REQUIRED"));
        assertThat(importedExecution.getPriority(), is(0));
        assertThat(importedExecution.isAutheticatorFlow(), is(false));

        importedExecution = getExecutionFromFlow(unchangedFlow, "http-basic-authenticator");
        assertThat(importedExecution.getAuthenticator(), is("http-basic-authenticator"));
        assertThat(importedExecution.getRequirement(), is("DISABLED"));
        assertThat(importedExecution.getPriority(), is(1));
        assertThat(importedExecution.isAutheticatorFlow(), is(false));
    }

    @Test
    @Order(3)
    void shouldChangeExecutionPriorities() {
        doImport("3_update_realm__change_execution_priorities.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        AuthenticationFlowRepresentation unchangedFlow = getAuthenticationFlow(updatedRealm, "my auth flow");
        assertThat(unchangedFlow.getDescription(), is("My auth flow for testing"));
        assertThat(unchangedFlow.getProviderId(), is("basic-flow"));
        assertThat(unchangedFlow.isBuiltIn(), is(false));
        assertThat(unchangedFlow.isTopLevel(), is(true));

        List<AuthenticationExecutionExportRepresentation> importedExecutions = unchangedFlow.getAuthenticationExecutions();
        assertThat(importedExecutions, hasSize(2));

        AuthenticationExecutionExportRepresentation importedExecution = getExecutionFromFlow(unchangedFlow, "docker-http-basic-authenticator");
        assertThat(importedExecution.getAuthenticator(), is("docker-http-basic-authenticator"));
        assertThat(importedExecution.getRequirement(), is("REQUIRED"));
        assertThat(importedExecution.getPriority(), is(1));
        assertThat(importedExecution.isAutheticatorFlow(), is(false));

        importedExecution = getExecutionFromFlow(unchangedFlow, "http-basic-authenticator");
        assertThat(importedExecution.getAuthenticator(), is("http-basic-authenticator"));
        assertThat(importedExecution.getRequirement(), is("DISABLED"));
        assertThat(importedExecution.getPriority(), is(0));
        assertThat(importedExecution.isAutheticatorFlow(), is(false));
    }

    @Test
    @Order(4)
    void shouldAddFlowWithExecutionFlow() {
        doImport("4_update_realm__add_flow_with_execution_flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my registration");
        assertThat(topLevelFlow.getDescription(), is("My registration flow"));
        assertThat(topLevelFlow.getProviderId(), is("basic-flow"));
        assertThat(topLevelFlow.isBuiltIn(), is(false));
        assertThat(topLevelFlow.isTopLevel(), is(true));

        List<AuthenticationExecutionExportRepresentation> executionFlows = topLevelFlow.getAuthenticationExecutions();
        assertThat(executionFlows, hasSize(1));

        AuthenticationExecutionExportRepresentation execution = getExecutionFromFlow(topLevelFlow, "registration-page-form");
        assertThat(execution.getAuthenticator(), is("registration-page-form"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(0));
        assertThat(execution.isAutheticatorFlow(), is(true));

        AuthenticationFlowRepresentation nonTopLevelFlow = getAuthenticationFlow(updatedRealm, "my registration form");

        List<AuthenticationExecutionExportRepresentation> nonTopLevelFlowExecutions = nonTopLevelFlow.getAuthenticationExecutions();
        assertThat(nonTopLevelFlowExecutions, hasSize(2));

        execution = getExecutionFromFlow(nonTopLevelFlow, "registration-user-creation");
        assertThat(execution.getAuthenticator(), is("registration-user-creation"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(0));
        assertThat(execution.isAutheticatorFlow(), is(false));

        execution = getExecutionFromFlow(nonTopLevelFlow, "registration-profile-action");
        assertThat(execution.getAuthenticator(), is("registration-profile-action"));
        assertThat(execution.getRequirement(), is("DISABLED"));
        assertThat(execution.getPriority(), is(1));
        assertThat(execution.isAutheticatorFlow(), is(false));
    }

    @Test
    @Order(41)
    void shouldFailWhenTryAddFlowWithDefectiveExecutionFlow() {
        RealmImport foundImport = getImport("4.1_try_to_update_realm__add_flow_with_defective_execution_flow.json");

        catchException(realmImportService).doImport(foundImport);

        assertThat(caughtException(),
                allOf(
                        instanceOf(ImportProcessingException.class),
                        CatchExceptionHamcrestMatchers.hasMessage("Cannot create execution-flow 'my registration form' for top-level-flow 'my registration' for realm 'realmWithFlow'")
                )
        );
    }

    @Test
    @Order(50)
    void shouldChangeFlowRequirementWithExecutionFlow() {
        doImport("5_update_realm__change_requirement_flow_with_execution_flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my registration");
        assertThat(topLevelFlow.getDescription(), is("My registration flow"));
        assertThat(topLevelFlow.getProviderId(), is("basic-flow"));
        assertThat(topLevelFlow.isBuiltIn(), is(false));
        assertThat(topLevelFlow.isTopLevel(), is(true));

        List<AuthenticationExecutionExportRepresentation> executionFlows = topLevelFlow.getAuthenticationExecutions();
        assertThat(executionFlows, hasSize(1));

        AuthenticationExecutionExportRepresentation execution = getExecutionFromFlow(topLevelFlow, "registration-page-form");
        assertThat(execution.getAuthenticator(), is("registration-page-form"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(0));
        assertThat(execution.isAutheticatorFlow(), is(true));

        AuthenticationFlowRepresentation nonTopLevelFlow = getAuthenticationFlow(updatedRealm, "my registration form");

        List<AuthenticationExecutionExportRepresentation> nonTopLevelFlowExecutions = nonTopLevelFlow.getAuthenticationExecutions();
        assertThat(nonTopLevelFlowExecutions, hasSize(2));

        execution = getExecutionFromFlow(nonTopLevelFlow, "registration-user-creation");
        assertThat(execution.getAuthenticator(), is("registration-user-creation"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(0));
        assertThat(execution.isAutheticatorFlow(), is(false));

        execution = getExecutionFromFlow(nonTopLevelFlow, "registration-profile-action");
        assertThat(execution.getAuthenticator(), is("registration-profile-action"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(1));
        assertThat(execution.isAutheticatorFlow(), is(false));
    }

    @Test
    @Order(51)
    void shouldFailWhenTryToUpdateDefectiveFlowRequirementWithExecutionFlow() {
        RealmImport foundImport = getImport("5.1_try_to_update_realm__change_requirement_in defective_flow_with_execution_flow.json");

        catchException(realmImportService).doImport(foundImport);

        assertThat(caughtException(),
                allOf(
                        instanceOf(ImportProcessingException.class),
                        CatchExceptionHamcrestMatchers.hasMessage("Cannot create execution-flow 'my registration form' for top-level-flow 'my registration' for realm 'realmWithFlow'")
                )
        );
    }

    @Test
    @Order(52)
    void shouldFailWhenTryToUpdateFlowRequirementWithExecutionFlowWithNotExistingExecution() throws Exception {
        RealmImport foundImport = getImport("5.2_try_to_update_realm__change_requirement_flow_with_execution_flow_with_not_existing_execution.json");

        catchException(realmImportService).doImport(foundImport);

        assertThat(caughtException(),
                allOf(
                        instanceOf(ImportProcessingException.class),
                        CatchExceptionHamcrestMatchers.hasMessage("Cannot create execution 'not-existing-registration-user-creation' for non-top-level-flow 'my registration form' for realm 'realmWithFlow'")
                )
        );
    }

    @Test
    @Order(53)
    void shouldFailWhenTryToUpdateFlowRequirementWithExecutionFlowWithDefectiveExecution() {
        RealmImport foundImport = getImport("5.3_try_to_update_realm__change_requirement_flow_with_execution_flow_with_defective_execution.json");

        catchException(realmImportService).doImport(foundImport);

        assertThat(caughtException(),
                allOf(
                        instanceOf(ImportProcessingException.class),
                        CatchExceptionHamcrestMatchers.hasMessage("Cannot update execution-flow 'registration-user-creation' for flow 'my registration form' for realm 'realmWithFlow'")
                )
        );
    }

    @Test
    @Order(54)
    void shouldFailWhenTryToUpdateFlowRequirementWithDefectiveExecutionFlow() {
        RealmImport foundImport = getImport("5.4_try_to_update_realm__change_requirement_flow_with_defective_execution_flow.json");

        catchException(realmImportService).doImport(foundImport);

        assertThat(caughtException(),
                allOf(
                        instanceOf(ImportProcessingException.class),
                        CatchExceptionHamcrestMatchers.hasMessage("Cannot create execution-flow 'docker-http-basic-authenticator' for top-level-flow 'my auth flow' for realm 'realmWithFlow'")
                )
        );
    }

    @Test
    @Order(60)
    void shouldChangeFlowPriorityWithExecutionFlow() {
        doImport("6_update_realm__change_priority_flow_with_execution_flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my registration");
        assertThat(topLevelFlow.getDescription(), is("My registration flow"));
        assertThat(topLevelFlow.getProviderId(), is("basic-flow"));
        assertThat(topLevelFlow.isBuiltIn(), is(false));
        assertThat(topLevelFlow.isTopLevel(), is(true));

        List<AuthenticationExecutionExportRepresentation> executionFlows = topLevelFlow.getAuthenticationExecutions();
        assertThat(executionFlows, hasSize(1));

        AuthenticationExecutionExportRepresentation execution = getExecutionFromFlow(topLevelFlow, "registration-page-form");
        assertThat(execution.getAuthenticator(), is("registration-page-form"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(0));
        assertThat(execution.isAutheticatorFlow(), is(true));

        AuthenticationFlowRepresentation nonTopLevelFlow = getAuthenticationFlow(updatedRealm, "my registration form");

        List<AuthenticationExecutionExportRepresentation> nonTopLevelFlowExecutions = nonTopLevelFlow.getAuthenticationExecutions();
        assertThat(nonTopLevelFlowExecutions, hasSize(2));

        execution = getExecutionFromFlow(nonTopLevelFlow, "registration-user-creation");
        assertThat(execution.getAuthenticator(), is("registration-user-creation"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(1));
        assertThat(execution.isAutheticatorFlow(), is(false));

        execution = getExecutionFromFlow(nonTopLevelFlow, "registration-profile-action");
        assertThat(execution.getAuthenticator(), is("registration-profile-action"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(0));
        assertThat(execution.isAutheticatorFlow(), is(false));
    }

    @Test
    @Order(70)
    void shouldSetRegistrationFlow() {
        doImport("7_update_realm__set_registration_flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getRegistrationFlow(), is("my registration"));
    }

    @Test
    @Order(80)
    void shouldChangeRegistrationFlow() {
        doImport("8_update_realm__change_registration_flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getRegistrationFlow(), is("my registration"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my registration");
        assertThat(topLevelFlow.getDescription(), is("My changed registration flow"));
    }

    @Test
    @Order(90)
    void shouldAddAndSetResetCredentialsFlow() {
        doImport("9_update_realm__add_and_set_custom_reset-credentials-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getResetCredentialsFlow(), is("my reset credentials"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my reset credentials");
        assertThat(topLevelFlow.getDescription(), is("My reset credentials for a user if they forgot their password or something"));
    }

    @Test
    @Order(100)
    void shouldChangeResetCredentialsFlow() {
        doImport("10_update_realm__change_custom_reset-credentials-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getResetCredentialsFlow(), is("my reset credentials"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my reset credentials");
        assertThat(topLevelFlow.getDescription(), is("My changed reset credentials for a user if they forgot their password or something"));
    }

    @Test
    @Order(110)
    void shouldAddAndSetBrowserFlow() {
        doImport("11_update_realm__add_and_set_custom_browser-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getBrowserFlow(), is("my browser"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my browser");
        assertThat(topLevelFlow.getDescription(), is("My browser based authentication"));
    }

    private void shouldChangeBrowserFlow() {
        doImport("12_update_realm__change_custom_browser-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getBrowserFlow(), is("my browser"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my browser");
        assertThat(topLevelFlow.getDescription(), is("My changed browser based authentication"));
    }

    @Test
    @Order(130)
    void shouldAddAndSetDirectGrantFlow() {
        doImport("13_update_realm__add_and_set_custom_direct-grant-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getDirectGrantFlow(), is("my direct grant"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my direct grant");
        assertThat(topLevelFlow.getDescription(), is("My OpenID Connect Resource Owner Grant"));
    }

    @Test
    @Order(140)
    void shouldChangeDirectGrantFlow() {
        doImport("14_update_realm__change_custom_direct-grant-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getDirectGrantFlow(), is("my direct grant"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my direct grant");
        assertThat(topLevelFlow.getDescription(), is("My changed OpenID Connect Resource Owner Grant"));
    }

    @Test
    @Order(150)
    void shouldAddAndSetClientAuthenticationFlow() {
        doImport("15_update_realm__add_and_set_custom_client-authentication-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getClientAuthenticationFlow(), is("my clients"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my clients");
        assertThat(topLevelFlow.getDescription(), is("My Base authentication for clients"));
    }

    @Test
    @Order(160)
    void shouldChangeClientAuthenticationFlow() {
        doImport("16_update_realm__change_custom_client-authentication-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getClientAuthenticationFlow(), is("my clients"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my clients");
        assertThat(topLevelFlow.getDescription(), is("My changed Base authentication for clients"));
    }

    @Test
    @Order(170)
    void shouldAddAndSetDockerAuthenticationFlow() {
        doImport("17_update_realm__add_and_set_custom_docker-authentication-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getDockerAuthenticationFlow(), is("my docker auth"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my docker auth");
        assertThat(topLevelFlow.getDescription(), is("My Used by Docker clients to authenticate against the IDP"));
    }

    @Test
    @Order(180)
    void shouldChangeDockerAuthenticationFlow() {
        doImport("18_update_realm__change_custom_docker-authentication-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        assertThat(updatedRealm.getDockerAuthenticationFlow(), is("my docker auth"));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my docker auth");
        assertThat(topLevelFlow.getDescription(), is("My changed Used by Docker clients to authenticate against the IDP"));
    }

    @Test
    @Order(190)
    void shouldAddTopLevelFlowWithExecutionFlow() {
        doImport("19_update_realm__add-top-level-flow-with-execution-flow.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        assertThat(updatedRealm.getRealm(), is(REALM_NAME));
        assertThat(updatedRealm.isEnabled(), is(true));

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my auth flow with execution-flows");
        assertThat(topLevelFlow.getDescription(), is("My authentication flow with authentication executions"));
        assertThat(topLevelFlow.getProviderId(), is("basic-flow"));
        assertThat(topLevelFlow.isBuiltIn(), is(false));
        assertThat(topLevelFlow.isTopLevel(), is(true));

        AuthenticationFlowRepresentation nonTopLevelFlow = getAuthenticationFlow(updatedRealm, "my execution-flow");

        List<AuthenticationExecutionExportRepresentation> nonTopLevelFlowExecutions = nonTopLevelFlow.getAuthenticationExecutions();
        assertThat(nonTopLevelFlowExecutions, hasSize(2));

        AuthenticationExecutionExportRepresentation execution = getExecutionFromFlow(nonTopLevelFlow, "auth-username-password-form");
        assertThat(execution.getAuthenticator(), is("auth-username-password-form"));
        assertThat(execution.getRequirement(), is("REQUIRED"));
        assertThat(execution.getPriority(), is(0));
        assertThat(execution.isAutheticatorFlow(), is(false));

        execution = getExecutionFromFlow(nonTopLevelFlow, "auth-otp-form");
        assertThat(execution.getAuthenticator(), is("auth-otp-form"));
        assertThat(execution.getRequirement(), is("CONDITIONAL"));
        assertThat(execution.getPriority(), is(1));
        assertThat(execution.isAutheticatorFlow(), is(false));
    }

    @Test
    @Order(200)
    void shouldUpdateTopLevelFlowWithPseudoId() {
        doImport("20_update_realm__update-top-level-flow-with-pseudo-id.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        AuthenticationFlowRepresentation topLevelFlow = getAuthenticationFlow(updatedRealm, "my auth flow");
        assertThat(topLevelFlow.getDescription(), is("My auth flow for testing with pseudo-id"));
    }

    @Test
    @Order(210)
    void shouldUpdateNonTopLevelFlowWithPseudoId() {
        doImport("21_update_realm__update-non-top-level-flow-with-pseudo-id.json");

        RealmRepresentation updatedRealm = keycloakProvider.get().realm(REALM_NAME).partialExport(true, true);

        AuthenticationFlowRepresentation nonTopLevelFlow = getAuthenticationFlow(updatedRealm, "my registration form");
        assertThat(nonTopLevelFlow.getDescription(), is("My registration form with pseudo-id"));
    }

    @Test
    @Order(220)
    void shouldFailWhenTryingToUpdateBuiltInFlow() {
        RealmImport foundImport = getImport("22_update_realm__try-to-update-built-in-flow.json");

        catchException(realmImportService).doImport(foundImport);

        assertThat(caughtException(),
                allOf(
                        instanceOf(InvalidImportException.class),
                        CatchExceptionHamcrestMatchers.hasMessage("Unable to recreate flow 'clients' in realm 'realmWithFlow': Deletion or creation of built-in flows is not possible")
                )
        );
    }

    private AuthenticationExecutionExportRepresentation getExecutionFromFlow(AuthenticationFlowRepresentation unchangedFlow, String executionAuthenticator) {
        List<AuthenticationExecutionExportRepresentation> importedExecutions = unchangedFlow.getAuthenticationExecutions();

        Optional<AuthenticationExecutionExportRepresentation> maybeImportedExecution = importedExecutions.stream()
                .filter(e -> e.getAuthenticator().equals(executionAuthenticator))
                .findFirst();

        assertThat(maybeImportedExecution.isPresent(), is(true));

        return maybeImportedExecution.get();
    }

    private AuthenticationFlowRepresentation getAuthenticationFlow(RealmRepresentation updatedRealm, String flowAlias) {
        List<AuthenticationFlowRepresentation> authenticationFlows = updatedRealm.getAuthenticationFlows();
        Optional<AuthenticationFlowRepresentation> maybeImportedFlow = authenticationFlows.stream()
                .filter(f -> f.getAlias().equals(flowAlias))
                .findFirst();

        assertThat("Cannot find authentication-flow '" + flowAlias + "'", maybeImportedFlow.isPresent(), is(true));

        return maybeImportedFlow.get();
    }

    private void doImport(String realmImport) {
        importUtil.doImport(realmImport);
    }

    private RealmImport getImport(String importName) {
        return importUtil.getImport(importName);
    }
}
