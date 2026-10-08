FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /src
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q test package
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /src/target/meet-api-0.1.0.jar app.jar
RUN mkdir -p /data/uploads && chown -R 10001:10001 /data
EXPOSE 8080
USER 10001
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
