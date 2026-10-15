FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY sistema/pom.xml ./pom.xml
COPY sistema/src/main ./src/main
# application.properties local é excluído pelo .dockerignore.
# Estes alvos compilam o artefato; os testes são executados nos gates focados.
RUN mvn -B -ntp compile dependency:copy-dependencies -DincludeScope=runtime

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /build/target/classes ./classes
COPY --from=build /build/target/dependency ./lib
COPY --chmod=755 deploy/internet/entrypoint.sh ./entrypoint.sh
# Render disponibiliza Secret Files para leitura pelo grupo1000.
USER 10001:1000
ENV PORT=10000
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65.0 -XX:InitialRAMPercentage=15.0"
EXPOSE 10000
ENTRYPOINT ["/app/entrypoint.sh"]
