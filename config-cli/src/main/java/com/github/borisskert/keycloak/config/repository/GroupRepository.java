package com.github.borisskert.keycloak.config.repository;

import com.github.borisskert.keycloak.config.util.CloneUtils;
import com.github.borisskert.keycloak.config.util.ResponseUtil;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GroupRepository {

    private final RealmRepository realmRepository;
    private final RoleRepository roleRepository;
    private final ClientRepository clientRepository;

    @Autowired
    public GroupRepository(
            RealmRepository realmRepository,
            RoleRepository roleRepository,
            ClientRepository clientRepository
    ) {
        this.realmRepository = realmRepository;
        this.roleRepository = roleRepository;
        this.clientRepository = clientRepository;
    }

    public Optional<GroupRepresentation> tryToFindGroupByPath(String realm, String groupPath) {
        GroupsResource groupsResource = realmRepository.loadRealm(realm)
                .groups();

        return groupsResource.groups()
                .stream()
                .filter(g -> Objects.equals(g.getPath(), groupPath))
                .findFirst();
    }

    public Optional<GroupRepresentation> tryToFindGroupByName(String realm, String groupName) {
        GroupsResource groupsResource = realmRepository.loadRealm(realm)
                .groups();

        return groupsResource.groups()
                .stream()
                .filter(g -> Objects.equals(g.getName(), groupName))
                .findFirst();
    }

    public void createGroup(String realm, GroupRepresentation group) {
        Response response = realmRepository.loadRealm(realm)
                .groups()
                .add(group);

        ResponseUtil.throwOnError(response);

        GroupRepresentation existingGroup = loadGroupByName(realm, group.getName()).toRepresentation();
        GroupRepresentation patchedGroup = CloneUtils.patch(existingGroup, group);

        addRealmRoles(realm, patchedGroup);
        addClientRoles(realm, patchedGroup);
        addSubGroups(realm, patchedGroup);
    }

    private void addSubGroups(String realm, GroupRepresentation existingGroup) {
        List<GroupRepresentation> subGroups = existingGroup.getSubGroups();

        if (subGroups != null && !subGroups.isEmpty()) {
            for (GroupRepresentation subGroup : subGroups) {
                addSubGroup(realm, existingGroup.getId(), subGroup);
            }
        }
    }

    private void addSubGroup(String realm, String parentGroupId, GroupRepresentation subGroup) {
        GroupResource groupResource = loadGroupById(realm, parentGroupId);
        Response response = groupResource.subGroup(subGroup);

        ResponseUtil.throwOnError(response);

        GroupRepresentation existingSubGroup = loadSubGroupByName(realm, parentGroupId, subGroup.getName());
        GroupRepresentation patchedGroup = CloneUtils.patch(existingSubGroup, subGroup);

        addRealmRoles(realm, patchedGroup);
        addClientRoles(realm, patchedGroup);
        addSubGroups(realm, patchedGroup);
    }

    private void updateSubGroup(String realm, String parentGroupId, GroupRepresentation subGroup) {
        GroupRepresentation existingSubGroup = loadSubGroupByName(realm, parentGroupId, subGroup.getName());

        GroupRepresentation patchedSubGroup = CloneUtils.patch(existingSubGroup, subGroup);
        persistGroup(realm, patchedSubGroup);

        String subGroupId = existingSubGroup.getId();

        List<String> realmRoles = subGroup.getRealmRoles();
        if (realmRoles != null) {
            updateGroupRealmRoles(realm, subGroupId, realmRoles);
        }

        Map<String, List<String>> clientRoles = subGroup.getClientRoles();
        if (clientRoles != null) {
            updateGroupClientRoles(realm, subGroupId, clientRoles);
        }

        List<GroupRepresentation> subGroups = subGroup.getSubGroups();
        if (subGroups != null) {
            updateSubGroups(realm, subGroupId, subGroups);
        }
    }

    private GroupRepresentation loadSubGroupByName(String realm, String parentGroupId, String name) {
        GroupRepresentation existingGroup = loadGroupById(realm, parentGroupId).toRepresentation();

        return existingGroup.getSubGroups()
                .stream()
                .filter(subgroup -> Objects.equals(subgroup.getName(), name))
                .findFirst()
                .get();
    }

    private void addClientRoles(String realm, GroupRepresentation existingGroup) {
        Map<String, List<String>> existingClientRoles = existingGroup.getClientRoles();
        GroupResource groupResource = loadGroupById(realm, existingGroup.getId());

        if (existingClientRoles != null && !existingClientRoles.isEmpty()) {

            for (Map.Entry<String, List<String>> existingClientRolesEntry : existingClientRoles.entrySet()) {
                String clientId = existingClientRolesEntry.getKey();
                List<String> clientRoleNames = existingClientRolesEntry.getValue();

                List<RoleRepresentation> actualClientRoles = roleRepository.searchClientRoles(realm, clientId, clientRoleNames);

                ClientRepresentation client = clientRepository.getClient(realm, clientId);
                RoleScopeResource groupClientRolesResource = groupResource.roles().clientLevel(client.getId());

                groupClientRolesResource.add(actualClientRoles);
            }
        }
    }

    private void addRealmRoles(String realm, GroupRepresentation existingGroup) {
        List<String> realmRoles = existingGroup.getRealmRoles();

        if (realmRoles != null && !realmRoles.isEmpty()) {
            GroupResource groupResource = loadGroupById(realm, existingGroup.getId());
            RoleMappingResource groupRoles = groupResource.roles();
            RoleScopeResource groupRealmRoles = groupRoles.realmLevel();

            List<RoleRepresentation> existingRealmRoles = realmRoles.stream()
                    .map(realmRole -> roleRepository.findRealmRole(realm, realmRole))
                    .collect(Collectors.toList());

            groupRealmRoles.add(existingRealmRoles);
        }
    }

    public void updateGroup(String realm, GroupRepresentation group) {
        GroupRepresentation existingGroup = loadGroupByName(realm, group.getName())
                .toRepresentation();

        GroupRepresentation patchedGroup = CloneUtils.patch(existingGroup, group);

        persistGroup(realm, patchedGroup);

        String groupId = existingGroup.getId();

        List<String> realmRoles = group.getRealmRoles();
        if (realmRoles != null) {
            updateGroupRealmRoles(realm, groupId, realmRoles);
        }

        Map<String, List<String>> clientRoles = group.getClientRoles();
        if (clientRoles != null) {
            updateGroupClientRoles(realm, groupId, clientRoles);
        }

        List<GroupRepresentation> subGroups = group.getSubGroups();
        if (subGroups != null) {
            updateSubGroups(realm, patchedGroup.getId(), subGroups);
        }
    }

    private void updateSubGroups(String realm, String parentGroupId, List<GroupRepresentation> subGroups) {
        GroupRepresentation existingGroup = loadGroupById(realm, parentGroupId).toRepresentation();

        List<GroupRepresentation> existingSubGroups = existingGroup.getSubGroups();

        for (GroupRepresentation existingSubGroup : existingSubGroups) {
            if (!hasGroupWithName(subGroups, existingSubGroup.getName())) {
                deleteGroup(realm, existingSubGroup.getId());
            }
        }

        for (GroupRepresentation subGroup : subGroups) {
            if (!hasGroupWithName(existingSubGroups, subGroup.getName())) {
                addSubGroup(realm, parentGroupId, subGroup);
            } else {
                updateSubGroup(realm, parentGroupId, subGroup);
            }
        }
    }

    private void deleteGroup(String realm, String id) {
        GroupResource groupResource = loadGroupById(realm, id);
        groupResource.remove();
    }

    private boolean hasGroupWithName(List<GroupRepresentation> groups, String groupName) {
        return groups.stream().anyMatch(g -> Objects.equals(g.getName(), groupName));
    }

    private void updateGroupRealmRoles(String realm, String groupId, List<String> realmRoles) {
        GroupResource groupResource = loadGroupById(realm, groupId);
        GroupRepresentation group = groupResource.toRepresentation();

        List<String> existingRealmRolesNames = group.getRealmRoles();
        List<String> realmRoleNamesToAdd = new ArrayList<>();
        List<String> realmRoleNamesToRemove = new ArrayList<>();

        for (String realmRoleName : realmRoles) {
            if (!existingRealmRolesNames.contains(realmRoleName)) {
                realmRoleNamesToAdd.add(realmRoleName);
            }
        }

        for (String existingRealmRolesName : existingRealmRolesNames) {
            if (!realmRoles.contains(existingRealmRolesName)) {
                realmRoleNamesToRemove.add(existingRealmRolesName);
            }
        }

        RoleMappingResource groupRoles = groupResource.roles();
        RoleScopeResource groupRealmRoles = groupRoles.realmLevel();
        List<RoleRepresentation> realmRoleToAdd = realmRoleNamesToAdd.stream()
                .map(name -> roleRepository.findRealmRole(realm, name))
                .collect(Collectors.toList());

        groupRealmRoles.add(realmRoleToAdd);

        List<RoleRepresentation> realmRoleToRemove = realmRoleNamesToRemove.stream()
                .map(name -> roleRepository.findRealmRole(realm, name))
                .collect(Collectors.toList());

        groupRealmRoles.remove(realmRoleToRemove);
    }

    private void updateGroupClientRoles(String realm, String groupId, Map<String, List<String>> groupClientRoles) {
        GroupResource groupResource = loadGroupById(realm, groupId);
        GroupRepresentation existingGroup = groupResource.toRepresentation();

        Map<String, List<String>> existingClientRoleNames = existingGroup.getClientRoles();

        for (Map.Entry<String, List<String>> existingClientRoleNamesEntry : existingClientRoleNames.entrySet()) {
            String clientId = existingClientRoleNamesEntry.getKey();
            List<String> clientRoleNames = existingClientRoleNamesEntry.getValue();

            if (!clientRoleNames.isEmpty() && !groupClientRoles.containsKey(clientId)) {
                removeClientRoles(realm, groupId, clientId, clientRoleNames);
            }
        }

        for (Map.Entry<String, List<String>> clientRole : groupClientRoles.entrySet()) {
            List<String> clientRoleNamesToAdd = new ArrayList<>();
            List<String> clientRoleNamesToRemove = new ArrayList<>();

            String clientId = clientRole.getKey();
            List<String> clientRoleNames = clientRole.getValue();

            List<String> existingClientRoleNamesForClient = existingClientRoleNames.get(clientId);

            for (String clientRoleName : clientRoleNames) {
                if (existingClientRoleNamesForClient == null || !existingClientRoleNamesForClient.contains(clientRoleName)) {
                    clientRoleNamesToAdd.add(clientRoleName);
                }
            }

            if (existingClientRoleNamesForClient != null) {
                for (String existingClientRoleNameForClient : existingClientRoleNamesForClient) {
                    if (!clientRoleNames.contains(existingClientRoleNameForClient)) {
                        clientRoleNamesToRemove.add(existingClientRoleNameForClient);
                    }
                }
            }

            addClientRoles(realm, groupId, clientId, clientRoleNamesToAdd);
            removeClientRoles(realm, groupId, clientId, clientRoleNamesToRemove);
        }
    }

    private void addClientRoles(String realm, String groupId, String clientId, List<String> roleNames) {
        System.out.println(new StringJoiner(" ").add(realm).add(groupId).add(clientId).add(roleNames.toString()));

        GroupResource groupResource = loadGroupById(realm, groupId);
        RoleMappingResource rolesResource = groupResource.roles();

        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        RoleScopeResource groupClientRolesResource = rolesResource.clientLevel(client.getId());

        List<RoleRepresentation> clientRoles = roleRepository.searchClientRoles(realm, clientId, roleNames);
        groupClientRolesResource.add(clientRoles);
    }

    private void removeClientRoles(String realm, String groupId, String clientId, List<String> roleNames) {
        System.out.println(new StringJoiner(" ").add(realm).add(groupId).add(clientId).add(roleNames.toString()));

        GroupResource groupResource = loadGroupById(realm, groupId);
        RoleMappingResource rolesResource = groupResource.roles();

        ClientRepresentation client = clientRepository.getClient(realm, clientId);
        RoleScopeResource groupClientRolesResource = rolesResource.clientLevel(client.getId());

        List<RoleRepresentation> clientRoles = roleRepository.searchClientRoles(realm, clientId, roleNames);
        groupClientRolesResource.remove(clientRoles);
    }

    private void persistGroup(String realm, GroupRepresentation group) {
        GroupResource groupResource = loadGroupById(realm, group.getId());
        groupResource.update(group);
    }

    private GroupResource loadGroupByName(String realm, String groupName) {
        Optional<GroupRepresentation> maybeGroup = tryToFindGroupByName(realm, groupName);

        GroupRepresentation existingGroup = maybeGroup.get();

        return loadGroupById(realm, existingGroup.getId());
    }

    private GroupResource loadGroupById(String realm, String groupId) {
        return realmRepository.loadRealm(realm)
                .groups()
                .group(groupId);
    }
}
