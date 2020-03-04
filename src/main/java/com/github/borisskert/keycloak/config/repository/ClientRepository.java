package com.github.borisskert.keycloak.config.repository;

import com.github.borisskert.keycloak.config.util.ResponseUtil;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.ClientsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ClientRepository {

    private final RealmRepository realmRepository;

    @Autowired
    public ClientRepository(RealmRepository realmRepository) {
        this.realmRepository = realmRepository;
    }

    public Optional<ClientRepresentation> tryToFindClient(String realm, String clientId) {
        Optional<ClientRepresentation> maybeClient;

        RealmResource realmResource = realmRepository.loadRealm(realm);
        ClientsResource clients = realmResource.clients();

        List<ClientRepresentation> foundClients = clients.findByClientId(clientId);

        if (foundClients.isEmpty()) {
            maybeClient = Optional.empty();
        } else {
            maybeClient = Optional.of(foundClients.get(0));
        }

        return maybeClient;
    }

    public ClientRepresentation getClient(String realm, String clientId) {
        return loadClient(realm, clientId);
    }

    public String getClientSecret(String realm, String clientId) {
        ClientResource clientResource = getClientResource(realm, clientId);
        return clientResource.getSecret().getValue();
    }

    public void create(String realm, ClientRepresentation clientToCreate) {
        RealmResource realmResource = realmRepository.loadRealm(realm);
        ClientsResource clientsResource = realmResource.clients();

        Response response = clientsResource.create(clientToCreate);
        ResponseUtil.throwOnError(response);
    }

    public void update(String realm, ClientRepresentation clientToUpdate) {
        RealmResource realmResource = realmRepository.loadRealm(realm);
        ClientsResource clientsResource = realmResource.clients();
        ClientResource clientResource = clientsResource.get(clientToUpdate.getId());

        clientResource.update(clientToUpdate);
    }


    private ClientRepresentation loadClient(String realm, String clientId) {
        List<ClientRepresentation> foundClients = realmRepository.loadRealm(realm)
                .clients()
                .findByClientId(clientId);

        if (foundClients.isEmpty()) {
            throw new RuntimeException("Cannot find client by clientId '" + clientId + "'");
        }

        return foundClients.get(0);
    }

    final ClientResource getClientResource(String realm, String clientId) {
        ClientRepresentation client = loadClient(realm, clientId);
        return realmRepository.loadRealm(realm)
                .clients()
                .get(client.getId());
    }

    public final Set<String> getClientIds(String realm) {
        return realmRepository.loadRealm(realm)
                .clients()
                .findAll()
                .stream()
                .map(ClientRepresentation::getClientId)
                .collect(Collectors.toSet());
    }

    public final List<ClientRepresentation> getClients(String realm) {
        return realmRepository.loadRealm(realm)
                .clients()
                .findAll();
    }
}
