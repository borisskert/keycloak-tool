package com.github.borisskert.keycloak.config.repository;

import com.github.borisskert.keycloak.config.util.ResponseUtil;
import org.keycloak.admin.client.resource.GroupResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import java.util.Optional;

@Service
public class GroupRepository {

    private final RealmRepository realmRepository;

    @Autowired
    public GroupRepository(RealmRepository realmRepository) {
        this.realmRepository = realmRepository;
    }

    public Optional<GroupRepresentation> tryToFindGroup(String realm, String groupPath) {
        GroupResource resource = realmRepository.loadRealm(realm)
                .groups()
                .group(groupPath);

        GroupRepresentation group;
        try {
            group = resource.toRepresentation();
        } catch (NotFoundException e) {
            return Optional.empty();
        }

        return Optional.of(group);
    }

    public void createGroup(String realm, GroupRepresentation group) {
        Response response = realmRepository.loadRealm(realm)
                .groups()
                .add(group);

        ResponseUtil.throwOnError(response);
    }
}
