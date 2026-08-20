# Etapa de compilacion
FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app
COPY pom.xml .

COPY src ./src
RUN mvn clean package -DskipTests -B

# Imagen de ejecucion
FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/concesionaria-api-1.0.0.jar app.jar

EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]