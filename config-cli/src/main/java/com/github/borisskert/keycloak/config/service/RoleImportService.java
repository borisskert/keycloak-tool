package com.github.borisskert.keycloak.config.service;

import com.github.borisskert.keycloak.config.model.RealmImport;
import com.github.borisskert.keycloak.config.repository.RoleRepository;
import com.github.borisskert.keycloak.config.util.CloneUtils;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.RolesRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RoleImportService {
    private static final Logger logger = LoggerFactory.getLogger(RoleImportService.class);

    private final RoleRepository roleRepository;

    @Autowired
    public RoleImportService(
            RoleRepository roleRepository
    ) {
        this.roleRepository = roleRepository;
    }

    public void doImport(RealmImport realmImport) {
        createOrUpdateRealmRoles(realmImport);
        createOrUpdateClientRoles(realmImport);

        updateRealmRoleComposites(realmImport);
        updateClientRoleComposites(realmImport);
    }

    private void createOrUpdateRealmRoles(RealmImport realmImport) {
        RolesRepresentation roles = realmImport.getRoles();
        List<RoleRepresentation> realmRoles = roles.getRealm();

        for (RoleRepresentation role : realmRoles) {
            createOrUpdateRealmRole(realmImport, role);
        }
    }

    private void createOrUpdateRealmRole(RealmImport realmImport, RoleRepresentation role) {
        String roleName = role.getName();
        String realm = realmImport.getRealm();

        Optional<RoleRepresentation> maybeRole = roleRepository.tryToFindRealmRole(realm, roleName);

        if (maybeRole.isPresent()) {
            logger.debug("Update realm-level role '{}' in realm '{}'", roleName, realm);
            updateRealmRole(realm, maybeRole.get(), role);
        } else {
            logger.debug("Create realm-level role '{}' in realm '{}'", roleName, realm);
            roleRepository.createRealmRole(realm, role);
        }
    }

    private void createOrUpdateClientRoles(RealmImport realmImport) {
        RolesRepresentation roles = realmImport.getRoles();
        Map<String, List<RoleRepresentation>> clientRolesPerClient = roles.getClient();

        for (Map.Entry<String, List<RoleRepresentation>> clientRoles : clientRolesPerClient.entrySet()) {
            createOrUpdateClientRoles(realmImport, clientRoles);
        }
    }

    private void createOrUpdateClientRoles(RealmImport realmImport, Map.Entry<String, List<RoleRepresentation>> clientRolesForClient) {
        String clientId = clientRolesForClient.getKey();
        List<RoleRepresentation> clientRoles = clientRolesForClient.getValue();

        for (RoleRepresentation role : clientRoles) {
            createOrUpdateClientRole(realmImport, clientId, role);
        }
    }

    private void updateRealmRoleComposites(RealmImport realmImport) {
        String realm = realmImport.getRealm();
        RolesRepresentation roles = realmImport.getRoles();
        List<RoleRepresentation> realmRoles = roles.getRealm();

        for (RoleRepresentation realmRole : realmRoles) {
            updateRealmRoleRealmCompositesIfNecessary(realm, realmRole);
            updateRealmRoleClientCompositesIfNecessary(realm, realmRole);
        }
    }

    private void updateClientRoleComposites(RealmImport realmImport) {
        String realm = realmImport.getRealm();
        RolesRepresentation roles = realmImport.getRoles();

        Map<String, List<RoleRepresentation>> clientRolesPerClient = roles.getClient();

        for (Map.Entry<String, List<RoleRepresentation>> clientRoles : clientRolesPerClient.entrySet()) {
            String clientId = clientRoles.getKey();

            for (RoleRepresentation clientRole : clientRoles.getValue()) {
                updateClientRoleRealmCompositesIfNecessary(realm, clientId, clientRole);
                updateClientRoleClientCompositesIfNecessary(realm, clientId, clientRole);
            }
        }
    }

    private void createOrUpdateClientRole(RealmImport realmImport, String clientId, RoleRepresentation role) {
        String roleName = role.getName();
        String realm = realmImport.getRealm();

        Optional<RoleRepresentation> maybeRole = roleRepository.tryToFindClientRole(realmImport.getRealm(), clientId, roleName);

        if (maybeRole.isPresent()) {
            logger.debug("Update client-level role '{}' for client '{}' in realm '{}'", roleName, clientId, realm);
            updateClientRole(realmImport.getRealm(), clientId, maybeRole.get(), role);
        } else {
            logger.debug("Create client-level role '{}' for client '{}' in realm '{}'", roleName, clientId, realm);
            roleRepository.createClientRole(realmImport.getRealm(), clientId, role);
        }
    }

    private void updateRealmRole(String realm, RoleRepresentation existingRole, RoleRepresentation roleToImport) {
        RoleRepresentation patchedRole = CloneUtils.deepPatch(existingRole, roleToImport);
        roleRepository.updateRealmRole(realm, patchedRole);
    }

    private void updateClientRole(String realm, String clientId, RoleRepresentation existingRole, RoleRepresentation roleToImport) {
        RoleRepresentation patchedRole = CloneUtils.deepPatch(existingRole, roleToImport);
        roleRepository.updateClientRole(realm, clientId, patchedRole);
    }

    private void updateRealmRoleRealmCompositesIfNecessary(String realm, RoleRepresentation realmRole) {
        Optional.ofNullable(realmRole.getComposites())
                .flatMap(composites -> Optional.ofNullable(composites.getRealm()))
                .ifPresent(realmComposites -> roleRepository.addRealmRoleRealmComposites(
                        realm,
                        realmRole.getName(),
                        realmComposites
                ));
    }

    private void updateClientRoleRealmCompositesIfNecessary(String realm, String roleClientId, RoleRepresentation clientRole) {
        Optional.ofNullable(clientRole.getComposites())
                .flatMap(composites -> Optional.ofNullable(composites.getRealm()))
                .ifPresent(realmComposites -> roleRepository.addClientRoleRealmComposites(
                        realm,
                        roleClientId,
                        clientRole.getName(),
                        realmComposites
                ));
    }

    private void updateRealmRoleClientCompositesIfNecessary(String realm, RoleRepresentation realmRole) {
        Optional.ofNullable(realmRole.getComposites())
                .flatMap(composites -> Optional.ofNullable(composites.getClient()))
                .ifPresent(clientComposites -> {
                    for (Map.Entry<String, List<String>> clientCompositesByClients : clientComposites.entrySet()) {
                        String clientId = clientCompositesByClients.getKey();
                        List<String> clientCompositesByClient = clientCompositesByClients.getValue();

                        roleRepository.addRealmRoleClientComposites(realm, realmRole.getName(), clientId, clientCompositesByClient);
                    }
                });
    }

    private void updateClientRoleClientCompositesIfNecessary(String realm, String roleClientId, RoleRepresentation clientRole) {
        Optional.ofNullable(clientRole.getComposites())
                .flatMap(composites -> Optional.ofNullable(composites.getClient()))
                .ifPresent(clientComposites -> {
                    for (Map.Entry<String, List<String>> clientCompositesByClients : clientComposites.entrySet()) {
                        String clientId = clientCompositesByClients.getKey();
                        List<String> clientCompositesByClient = clientCompositesByClients.getValue();

                        roleRepository.addClientRoleClientComposites(realm, roleClientId, clientRole.getName(), clientId, clientCompositesByClient);
                    }
                });
    }
}
