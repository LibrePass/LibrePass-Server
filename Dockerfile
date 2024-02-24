FROM eclipse-temurin:21-jdk as build

WORKDIR /workspace/app

COPY . .

RUN ./mvnw install -DskipTests -Dgpg.skip=true
RUN rm -r server/target/server-*-sources.jar
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../../server/target/*.jar)

FROM eclipse-temurin:21-jre

VOLUME /tmp

ARG DEPENDENCY=/workspace/app/target/dependency

COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

WORKDIR /app

ENTRYPOINT ["java", "-cp", ".:lib/*", "dev.medzik.librepass.server.ServerApplicationKt"]
