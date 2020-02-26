package com.github.borisskert.keycloak.config.repository;

import com.github.borisskert.keycloak.config.exception.KeycloakRepositoryException;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleRepository {

    private final RealmRepository realmRepository;
    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Autowired
    public RoleRepository(
            RealmRepository realmRepository,
            ClientRepository clientRepository,
            UserRepository userRepository
    ) {
        this.realmRepository = realmRepository;
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    public Optional<RoleRepresentation> tryToFindRealmRole(String realm, String name) {
        Optional<RoleRepresentation> maybeRole;

        RolesResource rolesResource = realmRepository.loadRealm(realm).roles();
        RoleResource roleResource = rolesResource.get(name);

        try {
            maybeRole = Optional.of(roleResource.toRepresentation());
        } catch (NotFoundException e) {
            maybeRole = Optional.empty();
        }

        return maybeRole;
    }

    public void createRealmRole(String realm, RoleRepresentation role) {
        RolesResource rolesResource = realmRepository.loadRealm(realm).roles();
        rolesResource.create(role);
    }

    public void addRealmRoleRealmComposites(String realm, String roleName, Set<String> realmComposites) {
        RoleResource roleResource = realmRepository.loadRealm(realm)
                .roles()
                .get(roleName);

        List<RoleRepresentation> realmRoles = realmComposites.stream()
                .map(realmRoleName -> findRealmRole(realm, realmRoleName))
                .collect(Collectors.toList());

        roleResource.addComposites(realmRoles);
    }

    public void addClientRoleRealmComposites(
            String realm,
            String roleClientId,
            String roleName,
            Set<String> realmComposites
    ) {
        ClientRepresentation client = clientRepository.getClient(realm, roleClientId);

        RoleResource roleResource = realmRepository.loadRealm(realm)
                .clients()
                .get(client.getId())
                .roles()
                .get(roleName);

        List<RoleRepresentation> realmRoles = realmComposites.stream()
                .map(realmRoleName -> findRealmRole(realm, realmRoleName))
                .collect(Collectors.toList());

        roleResource.addComposites(realmRoles);
    }

    public void addRealmRoleClientComposites(String realm, String roleName, String compositeClientId, Collection<String> clientRoles) {
        RoleResource roleResource = realmRepository.loadRealm(realm)
                .roles()
                .get(roleName);

        List<RoleRepresentation> realmRoles = clientRoles.stream()
                .map(clientRoleName -> findClientRole(realm, compositeClientId, clientRoleName))
                .collect(Collectors.toList());

        roleResource.addComposites(realmRoles);
    }

    public void addClientRoleClientComposites(
            String realm,
            String roleClientId,
            String roleName,
            String compositeClientId,
            Collection<String> clientComposites
    ) {
        ClientRepresentation client = clientRepository.getClient(realm, roleClientId);

        RoleResource roleResource = realmRepository.loadRealm(realm)
                .clients()
                .get(client.getId())
                .roles()
                .get(roleName);

        List<RoleRepresentation> clientRoles = clientComposites.stream()
                .map(clientRoleName -> findClientRole(realm, compositeClientId, clientRoleName))
                .collect(Collectors.toList());

        roleResource.addComposites(clientRoles);
    }

    public void updateRealmRole(String realm, RoleRepresentation roleToUpdate) {
        RoleResource roleResource = realmRepository.loadRealm(realm)
                .roles()
                .get(roleToUpdate.getName());

        roleResource.update(roleToUpdate);
    }

    public RoleRepresentation findRealmRole(String realm, String roleName) {
        return tryToFindRealmRole(realm, roleName)
                .orElseThrow(
                        () -> new KeycloakRepositoryException(
                                "Cannot find realm role '" + roleName + "' within realm '" + realm + "'"
                        )
                );
    }

    public List<RoleRepresentation> findRealmRoles(String realm, Collection<String> roles) {
        return roles.stream()
                .map(role -> findRealmRole(realm, role))
                .collect(Collectors.toList());
    }

    public Optional<RoleRepresentation> tryToFindClientRole(String realm, String clientId, String roleName) {
        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        RealmResource realmResource = realmRepository.loadRealm(realm);

        List<RoleRepresentation> clientRoles = realmResource.clients()
                .get(client.getId())
                .roles()
                .list();

        return clientRoles.stream()
                .filter(r -> r.getName().equals(roleName))
                .findFirst();
    }

    public RoleRepresentation findClientRole(String realm, String clientId, String roleName) {
        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        RealmResource realmResource = realmRepository.loadRealm(realm);

        List<RoleRepresentation> clientRoles = realmResource.clients()
                .get(client.getId())
                .roles()
                .list();

        return clientRoles.stream()
                .filter(r -> r.getName().equals(roleName))
                .findFirst()
                .get();
    }

    public List<RoleRepresentation> searchClientRoles(String realm, String clientId, List<String> roles) {
        ClientRepresentation foundClient = clientRepository.getClient(realm, clientId);

        ClientResource clientResource = realmRepository.loadRealm(realm)
                .clients()
                .get(foundClient.getId());

        return roles.stream()
                .map(role -> clientResource.roles()
                        .get(role)
                        .toRepresentation()
                ).collect(Collectors.toList());
    }

    public void createClientRole(String realm, String clientId, RoleRepresentation role) {
        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        RolesResource rolesResource = realmRepository.loadRealm(realm)
                .clients()
                .get(client.getId())
                .roles();

        rolesResource.create(role);
    }

    public void updateClientRole(String realm, String clientId, RoleRepresentation roleToUpdate) {
        ClientRepresentation client = clientRepository.getClient(realm, clientId);

        RoleResource roleResource = realmRepository.loadRealm(realm)
                .clients()
                .get(client.getId())
                .roles()
                .get(roleToUpdate.getName());

        roleResource.update(roleToUpdate);
    }

    public List<RoleRepresentation> searchRealmRoles(String realm, List<String> roles) {
        return roles.stream()
                .map(role -> realmRepository.loadRealm(realm)
                        .roles()
                        .get(role)
                        .toRepresentation()
                )
                .collect(Collectors.toList());
    }

    public List<String> getUserRealmLevelRoles(String realm, String username) {
        UserRepresentation user = userRepository.findUser(realm, username);
        UserResource userResource = realmRepository.loadRealm(realm)
                .users()
                .get(user.getId());

        List<RoleRepresentation> roles = userResource.roles()
                .realmLevel()
                .listEffective();

        return roles.stream().map(RoleRepresentation::getName).collect(Collectors.toList());
    }

    public void addRealmRolesToUser(String realm, String username, List<RoleRepresentation> realmRoles) {
        UserResource userResource = userRepository.getUserResource(realm, username);
        userResource.roles().realmLevel().add(realmRoles);
    }

    public void removeRealmRolesForUser(String realm, String username, List<RoleRepresentation> realmRoles) {
        UserResource userResource = userRepository.getUserResource(realm, username);
        userResource.roles().realmLevel().remove(realmRoles);
    }

    public void addClientRolesToUser(String realm, String username, String clientId, List<RoleRepresentation> clientRoles) {
        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        UserResource userResource = userRepository.getUserResource(realm, username);

        RoleScopeResource userClientRoles = userResource.roles()
                .clientLevel(client.getId());

        userClientRoles.add(clientRoles);
    }

    public void removeClientRolesForUser(String realm, String username, String clientId, List<RoleRepresentation> clientRoles) {
        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        UserResource userResource = userRepository.getUserResource(realm, username);

        RoleScopeResource userClientRoles = userResource.roles()
                .clientLevel(client.getId());

        userClientRoles.remove(clientRoles);
    }

    public List<String> getUserClientLevelRoles(String realm, String username, String clientId) {
        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        UserResource userResource = userRepository.getUserResource(realm, username);

        List<RoleRepresentation> roles = userResource.roles()
                .clientLevel(client.getId())
                .listEffective();

        return roles.stream().map(RoleRepresentation::getName).collect(Collectors.toList());
    }

    private RoleRepresentation getClientRole(String realm, String clientId, String roleName) {
        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        RealmResource realmResource = realmRepository.loadRealm(realm);

        List<RoleRepresentation> clientRoles = realmResource.clients()
                .get(client.getId())
                .roles()
                .list();

        return clientRoles.stream()
                .filter(r -> r.getName().equals(roleName))
                .findFirst()
                .get();
    }
}
