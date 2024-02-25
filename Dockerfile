FROM maven:latest as build

WORKDIR /workspace/build

ADD pom.xml /workspace/build
ADD client/pom.xml /workspace/build/client/pom.xml
ADD server/pom.xml /workspace/build/server/pom.xml
ADD shared/pom.xml /workspace/build/shared/pom.xml

RUN mvn dependency:go-offline

ADD . /workspace/build

RUN mvn install -DskipTests -Dgpg.skip=true
RUN cp server/target/server-*.jar /workspace/server.jar

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=build /workspace/server.jar /app

ENTRYPOINT ["java", "-jar", "/app/server.jar"]
