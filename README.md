[![Build Status](https://travis-ci.com/borisskert/keycloak-tool.svg?branch=master)](https://travis-ci.com/borisskert/keycloak-tool)
[![Maintainability](https://api.codeclimate.com/v1/badges/069a00d89c0b36a2f820/maintainability)](https://codeclimate.com/github/borisskert/keycloak-tool/maintainability) 
[![codecov](https://codecov.io/gh/borisskert/keycloak-tool/branch/master/graph/badge.svg)](https://codecov.io/gh/borisskert/keycloak-tool)


# keycloak-tools

~ is a tool to import realm configs into a running Keycloak instance.

#### Config files

The config files are based on the keycloak export files. You can use them to re-import your settings.
But keep your files as small as possible. Remove all UUIDs and all stuff which is default set by keycloak.

[moped.json](./example-config/moped.json) is a full working example file you can consider.
 Other examples are located in the [test resources](src/test/resources/import-files).

## Supported features

| Feature | Since | Description |
|---------|-------|-------------|
| Create clients | 0.9.0 | Create client configuration while creating or updating realms |
| Update clients | 0.9.0 | Update client configuration while updating realms             |
| Add roles      | 0.9.0 | Add roles while creating or updating realms                   |
| Update roles   | 0.9.0 | Update role properties while updating realms                  |
| Add composites to roles| 0.12.0 | Add role with realm-level and client-level composite roles while creating or updating realms                   |
| Add composites to roles | 0.12.0 | Add realm-level and client-level composite roles to existing role while creating or updating realms                   |
| Remove composites from roles | 0.12.0 | Remove realm-level and client-level composite roles from existing role while creating or updating realms                   |
| Add users      | 0.9.0 | Add users (inclusive password!) while creating or updating realms                   |
| Add users with roles | 0.9.0 | Add users with realm-level and client-level roles while creating or updating realms                   |
| Update users   | 0.9.0 | Update user properties (inclusive password!) while updating realms |
| Add role to user | 0.9.0 | Add realm-level and client-level roles to user while updating realm |
| Remove role from user | 0.9.0 | Remove realm-level or client-level roles from user while updating realm |
| Add authentication flows and executions | 0.9.0 | Add authentication flows and executions while creating or updating realms |
| Update authentication flows and executions | 0.9.0 | Update authentication flow properties and executions while updating realms |
| Add components | 0.9.0 | Add components while creating or updating realms                                                       |
| Update components | 0.9.0 | Update components properties while updating realms                                                       |
| Update sub-components | 0.9.0 | Add sub-components properties while creating or updating realms                                                       |
| Add groups | 0.12.0 | Add groups (inclusive subgroups!) to realm while creating or updating realms |
| Update groups | 0.12.0 | Update existing group properties and attributes while creating or updating realms |
| Remove groups | 0.12.0 | Remove existing groups while updating realms |
| Add/Remove group attributes | 0.12.0 | Add or remove group attributes in existing groups while updating realms |
| Add/Remove group roles | 0.12.0 | Add or remove roles to/from existing groups while updating realms |
| Update/Remove subgroups | 0.12.0 | Like groups, subgroups may also be added/updated and removed while updating realms |
| Add scope-mappings | 0.9.0 | Add scope-mappings while creating or updating realms |
| Add roles to scope-mappings | 0.9.0 | Add roles to existing scope-mappings while updating realms |
| Remove roles from scope-mappings | 0.9.0 | Remove roles from existing scope-mappings while updating realms |
| Add required-actions | 0.9.0 | Add required-actions while creating or updating realms |
| Update required-actions | 0.9.0 | Update properties of existing required-actions while updating realms |

Feel free to submit an issue if a feature is missing.

## Compatibility matrix

| keycloak-tools     | **Keycloak 4.4.0.Final** | **Keycloak 4.5.0.Final** | **Keycloak 4.6.0.Final** | **Keycloak 4.7.0.Final** | **Keycloak 4.8.3.Final** | **Keycloak 5.0.0** | **Keycloak 6.0.1** | **Keycloak 7.0.0** | **Keycloak 8.0.2** | **Keycloak 9.0.0** |
|--------------------|:------------------------:|:------------------------:|:------------------------:|:------------------------:|:------------------------:|:------------------:|:------------------:|:------------------:|:------------------:|:------------------:|
| **v0.9.0**         |         ✓                |         ✓                |         ✓                |         ✓                |         ✓                |         ✓          |         ✓          |         ✓          |         ✗          |         ✗          |
| **v0.10.1**        |         ✗                |         ✗                |         ✗                |         ✗                |         ✗                |         ✗          |         ✗          |         ✗          |         ✓          |         ✗          |
| **v0.11.0**        |         ✗                |         ✗                |         ✗                |         ✗                |         ✗                |         ✗          |         ✗          |         ✗          |         ✓          |         ✓          |
| **v0.12.0**        |         ✗                |         ✗                |         ✗                |         ✗                |         ✗                |         ✗          |         ✗          |         ✗          |         ✓          |         ✓          |
| **master**         |         ✗                |         ✗                |         ✗                |         ✗                |         ✗                |         ✗          |         ✗          |         ✗          |         ✓          |         ✓          |
- `✓` Supported
- `✗` Not supported


## Build and Test

Use maven:
```bash
$ mvn package
```

## Run this project

### via Java CLI

Start a local keycloak on port 8080:

```bash
$ docker-compose down --remove-orphans && docker-compose up keycloak
``` 

before performing import via keycloak-tool:

```bash
$ java -jar keycloak-tool.jar --keycloak.url=http://localhost:8080 --keycloak.password=admin123 --import.file=./example-config/moped.json
```

### Docker

#### Docker run

```
$ docker run -e KEYCLOAK_URL=http://<your keycloak host>:8080 \
             -e KEYCLOAK_ADMIN=<keycloak admin username> \
             -e KEYCLOAK_ADMIN_PASSWORD=<keycloak admin password> \
             -e WAIT_TIME_IN_SECONDS=120 \
             -e IMPORT_FORCE=false \
             -v <your config path>:/tmp/keycloak-tool/configs \
             borisskert/keycloak-tool:latest
```

#### Docker-compose

```
version: '3.1'
services:
  keycloak:
    image: jboss/keycloak:9.0.0
    environment:
      KEYCLOAK_USER: <keycloak admin username>
      KEYCLOAK_PASSWORD: <keycloak admin password>
    ports:
    - "8080:8080"
    networks:
    - my_network
    command:
    - "-b"
    - "0.0.0.0"
    - "--debug"
  keycloak_config:
    image: borisskert/keycloak-tool:latest
    depends_on:
    - keycloak
    links:
    - keycloak
    volumes:
    - <your config path>:/tmp/keycloak-tool/configs
    environment:
    - KEYCLOAK_URL=http://<your keycloak host>:8080/auth
    - KEYCLOAK_ADMIN=<keycloak admin username>
    - KEYCLOAK_ADMIN_PASSWORD=<keycloak admin password>
    - WAIT_TIME_IN_SECONDS=120
    - IMPORT_FORCE=false
    depends_on:
    - keycloak
    networks:
    - my_network

networks:
  my_network:

```

## Integration tests

```bash
$ mvn verify
```

## Links

* [Docker Hub 🐳](https://hub.docker.com/r/borisskert/keycloak-tool)
