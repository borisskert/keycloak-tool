package com.github.borisskert.keycloak.config.service;

import com.github.borisskert.keycloak.config.model.RealmImport;
import com.github.borisskert.keycloak.config.repository.GroupRepository;
import org.keycloak.representations.idm.GroupRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class GroupImportService {
    private static final Logger logger = LoggerFactory.getLogger(GroupImportService.class);

    private final GroupRepository groupRepository;

    public GroupImportService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public void importGroups(RealmImport realmImport) {
        List<GroupRepresentation> groups = realmImport.getGroups();

        if (groups == null) {
            logger.debug("No groups to import into realm '{}'", realmImport.getRealm());
        } else {
            List<GroupRepresentation> existingGroups = groupRepository.getGroups(realmImport.getRealm());

            if (groups.isEmpty()) {
                for (GroupRepresentation existingGroup : existingGroups) {
                    groupRepository.deleteGroup(realmImport.getRealm(), existingGroup.getId());
                }
            } else {
                for (GroupRepresentation existingGroup : existingGroups) {
                    if (!hasGroupWithName(groups, existingGroup.getName())) {
                        groupRepository.deleteGroup(realmImport.getRealm(), existingGroup.getId());
                    }
                }

                for (GroupRepresentation group : groups) {
                    createOrUpdateRealmGroup(realmImport.getRealm(), group);
                }
            }
        }
    }

    private boolean hasGroupWithName(List<GroupRepresentation> groups, String groupName) {
        return groups.stream().anyMatch(g -> Objects.equals(g.getName(), groupName));
    }

    private void createOrUpdateRealmGroup(String realm, GroupRepresentation group) {
        String groupName = group.getName();

        Optional<GroupRepresentation> maybeGroup = groupRepository.tryToFindGroupByName(realm, groupName);

        if (maybeGroup.isPresent()) {
            logger.debug("Update group '{}' in realm '{}'", groupName, realm);
            groupRepository.updateGroup(realm, group);
        } else {
            logger.debug("Create group '{}' in realm '{}'", groupName, realm);
            groupRepository.createGroup(realm, group);
        }
    }
}
