# Stage 1: Build the application
FROM maven:3.8.4-openjdk-8-slim AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn package -DskipTests

# Stage 2: Runtime environment
FROM openjdk:8-jre-slim
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Standard Spring Boot environment variables
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
