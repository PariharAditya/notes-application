# Maven for build
FROM maven:3-eclipse-temurin-21 AS build

LABEL application="API Gateway" 

WORKDIR /src

COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -e


FROM openjdk:21

WORKDIR /app

# Copy the JAR file to the correct location
COPY --from=build /src/target/*.jar /app/apiGateway.jar

EXPOSE 8181

HEALTHCHECK --interval=30s --timeout=3s \
    CMD wget -q --spider http://localhost:8181/actuator/health || exit 1

# Explicitly activate Docker profile
CMD ["java", "-Dspring.profiles.active=docker", "-jar", "/app/apiGateway.jar"]