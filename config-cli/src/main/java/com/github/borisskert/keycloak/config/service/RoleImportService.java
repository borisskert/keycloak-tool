package com.github.borisskert.keycloak.config.service;

import com.github.borisskert.keycloak.config.model.RealmImport;
import com.github.borisskert.keycloak.config.repository.ClientRepository;
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
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleImportService {
    private static final Logger logger = LoggerFactory.getLogger(RoleImportService.class);

    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public RoleImportService(
            RoleRepository roleRepository,
            ClientRepository clientRepository
    ) {
        this.roleRepository = roleRepository;
        this.clientRepository = clientRepository;
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
                .ifPresent(realmComposites -> updateRealmRoleRealmComposites(realm, realmRole, realmComposites));
    }

    private void updateRealmRoleRealmComposites(String realm, RoleRepresentation realmRole, Set<String> realmComposites) {
        String roleName = realmRole.getName();

        Set<String> existingRealmCompositeNames = findRealmRoleRealmCompositeNames(realm, roleName);

        removeRealmRoleRealmComposites(realm, roleName, existingRealmCompositeNames, realmComposites);
        addRealmRoleRealmComposites(realm, roleName, existingRealmCompositeNames, realmComposites);
    }

    private Set<String> findRealmRoleRealmCompositeNames(String realm, String roleName) {
        Set<RoleRepresentation> existingRealmComposites = roleRepository.findRealmRoleRealmComposites(realm, roleName);

        return existingRealmComposites.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> findClientRoleRealmCompositeNames(String realm, String roleClientId, String roleName) {
        Set<RoleRepresentation> existingRealmComposites = roleRepository.findClientRoleRealmComposites(realm, roleClientId, roleName);

        return existingRealmComposites.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }

    private void addRealmRoleRealmComposites(String realm, String roleName, Set<String> existingRealmCompositeNames, Set<String> realmComposites) {
        Set<String> realmCompositesToAdd = realmComposites.stream()
                .filter(name -> !existingRealmCompositeNames.contains(name))
                .collect(Collectors.toSet());

        roleRepository.addRealmRoleRealmComposites(
                realm,
                roleName,
                realmCompositesToAdd
        );
    }

    private void addClientRoleRealmComposites(String realm, String roleClientId, String roleName, Set<String> existingRealmCompositeNames, Set<String> realmComposites) {
        Set<String> realmCompositesToAdd = realmComposites.stream()
                .filter(name -> !existingRealmCompositeNames.contains(name))
                .collect(Collectors.toSet());

        roleRepository.addClientRoleRealmComposites(
                realm,
                roleClientId,
                roleName,
                realmCompositesToAdd
        );
    }

    private void removeRealmRoleRealmComposites(String realm, String roleName, Set<String> existingRealmCompositeNames, Set<String> realmComposites) {
        Set<String> realmCompositesToRemove = existingRealmCompositeNames.stream()
                .filter(name -> !realmComposites.contains(name))
                .collect(Collectors.toSet());

        roleRepository.removeRealmRoleRealmComposites(realm, roleName, realmCompositesToRemove);
    }

    private void removeClientRoleRealmComposites(String realm, String roleClientId, String roleName, Set<String> existingRealmCompositeNames, Set<String> realmComposites) {
        Set<String> realmCompositesToRemove = existingRealmCompositeNames.stream()
                .filter(name -> !realmComposites.contains(name))
                .collect(Collectors.toSet());

        roleRepository.removeClientRoleRealmComposites(realm, roleClientId, roleName, realmCompositesToRemove);
    }

    private void updateClientRoleRealmCompositesIfNecessary(String realm, String roleClientId, RoleRepresentation clientRole) {
        Optional.ofNullable(clientRole.getComposites())
                .flatMap(composites -> Optional.ofNullable(composites.getRealm()))
                .ifPresent(realmComposites -> updateClientRoleRealmComposites(realm, roleClientId, clientRole.getName(), realmComposites));
    }

    private void updateClientRoleRealmComposites(String realm, String roleClientId, String roleName, Set<String> realmComposites) {
        Set<String> existingRealmCompositeNames = findClientRoleRealmCompositeNames(realm, roleClientId, roleName);

        removeClientRoleRealmComposites(realm, roleClientId, roleName, existingRealmCompositeNames, realmComposites);
        addClientRoleRealmComposites(realm, roleClientId, roleName, existingRealmCompositeNames, realmComposites);
    }

    private void updateRealmRoleClientCompositesIfNecessary(String realm, RoleRepresentation realmRole) {
        Optional.ofNullable(realmRole.getComposites())
                .flatMap(composites -> Optional.ofNullable(composites.getClient()))
                .ifPresent(clientComposites -> updateRealmRoleClientComposites(realm, realmRole.getName(), clientComposites));
    }

    private void updateRealmRoleClientComposites(String realm, String realmRole, Map<String, List<String>> clientComposites) {
        for (Map.Entry<String, List<String>> clientCompositesByClients : clientComposites.entrySet()) {
            String clientId = clientCompositesByClients.getKey();
            List<String> clientCompositesByClient = clientCompositesByClients.getValue();

            Set<String> existingClientCompositeNames = findRealmRoleClientCompositeNames(realm, realmRole, clientId);

            removeRealmRoleClientComposites(realm, realmRole, clientId, existingClientCompositeNames, clientCompositesByClient);
            addRealmRoleClientComposites(realm, realmRole, clientId, existingClientCompositeNames, clientCompositesByClient);
        }

        removeRealmRoleClientComposites(realm, realmRole, clientComposites);
    }

    private void removeRealmRoleClientComposites(String realm, String realmRole, Map<String, List<String>> clientComposites) {
        Set<String> existingCompositeClients = clientRepository.getClientIds(realm);

        Set<String> compositeClientsToRemove = existingCompositeClients.stream()
                .filter(name -> !clientComposites.containsKey(name))
                .collect(Collectors.toSet());

        roleRepository.removeRealmRoleClientComposites(realm, realmRole, compositeClientsToRemove);
    }

    private void addRealmRoleClientComposites(String realm, String realmRole, String clientId, Set<String> existingClientCompositeNames, List<String> clientCompositesByClient) {
        Set<String> clientRoleCompositesToAdd = clientCompositesByClient.stream()
                .filter(name -> !existingClientCompositeNames.contains(name))
                .collect(Collectors.toSet());

        roleRepository.addRealmRoleClientComposites(realm, realmRole, clientId, clientRoleCompositesToAdd);
    }

    private Set<String> findRealmRoleClientCompositeNames(String realm, String realmRole, String clientId) {
        Set<RoleRepresentation> existingClientComposites = roleRepository.findRealmRoleClientComposites(realm, realmRole, clientId);

        return existingClientComposites.stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }

    private void removeRealmRoleClientComposites(String realm, String realmRole, String clientId, Set<String> existingClientCompositeNames, List<String> clientCompositesByClient) {
        Set<String> clientRoleCompositesToRemove = existingClientCompositeNames.stream()
                .filter(name -> !clientCompositesByClient.contains(name))
                .collect(Collectors.toSet());

        roleRepository.removeRealmRoleClientComposites(realm, realmRole, clientId, clientRoleCompositesToRemove);
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
