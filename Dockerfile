# Stage 1: Build the application
FROM eclipse-temurin:8-jdk AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline || true
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Runtime environment
FROM eclipse-temurin:8-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Standard Spring Boot environment variables
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# Hugging Face / Cloud Security Tweak (Non-Root User)
RUN groupadd -r appuser && useradd -r -g appuser appuser
USER appuser

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
