# Start with a base image containing Java runtime
FROM maven:3.5.3-jdk-8-alpine as target

COPY pom.xml /pom.xml

RUN mvn dependency:go-offline

RUN export FFA_P12_EXPORT_PASSWORD
RUN export keystore_password
RUN export ffa_authentication_username
RUN export ffa_authentication_password
# Add Maintainer Info
LABEL maintainer="vij@exilent.systems"

# Add a volume pointing to /tmp
VOLUME /src

RUN mkdir /target
# Make port 8020 available to the world outside this container

COPY .mvn /.mvn
COPY mvnw /mvnw
COPY mvnw.cmd /mvnw.cmd

COPY src /src
COPY ./entrypoint.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

