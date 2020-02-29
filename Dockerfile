FROM openjdk:8

WORKDIR /tmp/keycloak-tool
VOLUME /tmp/keycloak-tool/configs

COPY ./target/keycloak-tool.jar ./keycloak-tool.jar
COPY ./docker/root/ /

CMD /usr/local/bin/keycloak-tool
