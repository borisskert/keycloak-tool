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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

        if(subGroups != null && !subGroups.isEmpty()) {
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
        Map<String, List<String>> groupClientRoles = existingGroup.getClientRoles();

        if(groupClientRoles !=  null && !groupClientRoles.isEmpty()) {
            GroupResource groupResource = loadGroupById(realm, existingGroup.getId());

            for (Map.Entry<String, List<String>> clientRoles : groupClientRoles.entrySet()) {
                String clientId = clientRoles.getKey();
                List<String> clientRoleNames = clientRoles.getValue();

                List<RoleRepresentation> existingClientRoles = roleRepository.searchClientRoles(realm, clientId, clientRoleNames);

                ClientRepresentation client = clientRepository.getClient(realm, clientId);
                RoleScopeResource groupClientRolesResource = groupResource.roles().clientLevel(client.getId());

                groupClientRolesResource.add(existingClientRoles);
            }
        }
    }

    private void addRealmRoles(String realm, GroupRepresentation existingGroup) {
        List<String> realmRoles = existingGroup.getRealmRoles();

        if(realmRoles != null && !realmRoles.isEmpty()) {
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
        Optional<GroupRepresentation> maybeExistingGroup = tryToFindGroupByPath(realm, group.getPath());

        GroupRepresentation existingGroup = maybeExistingGroup.get();
        GroupRepresentation patchedGroup = CloneUtils.patch(existingGroup, group);

        persistGroup(realm, patchedGroup);
    }

    private void persistGroup(String realm, GroupRepresentation group) {
        GroupResource groupResource = loadGroupById(realm, group.getId());

        groupResource.toRepresentation();

        groupResource.update(group);
    }

    private GroupResource loadGroupByPath(String realm, String groupPath) {
        Optional<GroupRepresentation> maybeGroup = tryToFindGroupByPath(realm, groupPath);

        GroupRepresentation existingGroup = maybeGroup.get();

        return loadGroupById(realm, existingGroup.getId());
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
