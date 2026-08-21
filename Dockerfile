FROM maven:3.9.11-eclipse-temurin-21 AS build

WORKDIR /app
COPY concesionaria_/pom.xml .
COPY concesionaria_/src ./src
RUN mvn clean package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app
COPY --from=build /app/target/concesionaria-api-1.0.0.jar app.jar

EXPOSE 10000
ENTRYPOINT ["java", "-jar", "app.jar"]
