package com.github.borisskert.keycloak.config.repository;

import com.github.borisskert.keycloak.config.util.CloneUtils;
import com.github.borisskert.keycloak.config.util.ResponseUtil;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.admin.client.resource.GroupsResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.Objects;
import java.util.Optional;

@Service
public class GroupRepository {

    private final RealmRepository realmRepository;

    @Autowired
    public GroupRepository(RealmRepository realmRepository) {
        this.realmRepository = realmRepository;
    }

    public Optional<GroupRepresentation> tryToFindGroup(String realm, String groupPath) {
        GroupsResource groupsResource = realmRepository.loadRealm(realm)
                .groups();

        return groupsResource.groups()
                .stream()
                .filter(g -> Objects.equals(g.getPath(), groupPath))
                .findFirst();
    }

    public void createGroup(String realm, GroupRepresentation group) {
        Response response = realmRepository.loadRealm(realm)
                .groups()
                .add(group);

        ResponseUtil.throwOnError(response);
    }

    public void updateGroup(String realm, GroupRepresentation group) {
        Optional<GroupRepresentation> maybeExistingGroup = tryToFindGroup(realm, group.getPath());

        GroupRepresentation existingGroup = maybeExistingGroup.get();
        GroupRepresentation patchedGroup = CloneUtils.patch(existingGroup, group);

        persistGroup(realm, patchedGroup);
    }

    private void persistGroup(String realm, GroupRepresentation group) {
        GroupResource groupResource = loadGroup(realm, group.getId());

        groupResource.toRepresentation();

        groupResource.update(group);
    }

    private GroupResource loadGroup(String realm, String groupId) {
        return realmRepository.loadRealm(realm)
                .groups()
                .group(groupId);
    }
}
