FROM openjdk:8

WORKDIR /tmp/keycloak-tool
VOLUME /tmp/keycloak-tool/configs

COPY ./config-cli/target/config-cli.jar ./config-cli.jar
COPY ./docker/root/ /
