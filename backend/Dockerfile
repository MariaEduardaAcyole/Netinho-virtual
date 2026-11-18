# Primeiro estagio: compila e empacota a aplicacao usando Maven e JDK 25.
FROM maven:3.9.12-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Segundo estagio: executa o JAR em uma imagem menor, contendo apenas o JRE.
FROM eclipse-temurin:25-jre
# curl: usado pela verificacao de saude do Docker (GET /saude).
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/* \
    && useradd --system --no-create-home netinho
WORKDIR /app
COPY --from=build /app/target/netinho-1.0.0.jar app.jar
# A API nao precisa de root.
USER netinho
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD curl -fsS http://localhost:8080/saude || exit 1
ENTRYPOINT ["java","-jar","/app/app.jar"]
