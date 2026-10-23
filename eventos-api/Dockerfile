# Etapa 1: compilar el proyecto con Maven y Java 17.
FROM maven:3.9.11-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY src ./src
RUN mvn -B -ntp clean verify

# Etapa 2: ejecutar únicamente el JAR.
FROM eclipse-temurin:17-jre
WORKDIR /app
RUN groupadd --system appgroup && useradd --system --gid appgroup appuser \
    && mkdir -p /app/data && chown -R appuser:appgroup /app
COPY --from=build --chown=appuser:appgroup /build/target/eventos-api.jar /app/app.jar
USER appuser
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0"
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
