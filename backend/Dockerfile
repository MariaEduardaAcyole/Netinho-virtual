# Primeiro estagio: compila e empacota a aplicacao usando Maven e JDK 25.
FROM maven:3.9.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn package -DskipTests

# Segundo estagio: executa o JAR em uma imagem menor, contendo apenas o JRE.
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/netinho-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
