# Start with a base image containing Java runtime
FROM maven:3.5.3-jdk-8-alpine as target

COPY pom.xml /pom.xml

RUN mvn dependency:go-offline

# Add Maintainer Info
LABEL maintainer="vij@exilent.systems"

# Add a volume pointing to /tmp
VOLUME /src
RUN export FFA_P12_EXPORT_PASSWORD

RUN mkdir /target
# Make port 8020 available to the world outside this container
RUN export ffa_authentication_username
RUN export ffa_authentication_password

COPY .mvn /.mvn
COPY mvnw /mvnw
COPY mvnw.cmd /mvnw.cmd
COPY src /src
COPY entrypoint-production.sh /entrypoint.sh

RUN chmod +x /entrypoint.sh
ENTRYPOINT ["/entrypoint.sh"]

