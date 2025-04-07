#Maven with Eclipse Temurin JDK 21
FROM maven:3-eclipse-temurin-21 AS build

LABEL application="Notes Application"

# Set working directory
WORKDIR /app

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -e


# Second stage: runtime environment
FROM openjdk:21

WORKDIR /app

# Copy the JAR from the build stage
COPY --from=build /app/target/*.jar /app/notes-application.jar

# Create directory for Maven dependencies
COPY --from=build /root/.m2/repository/net/sf/jasperreports/ /app/jasperreports/

# Create the config directory
RUN mkdir -p /app/config

# Copy additional configuration
COPY src/main/resources/application-docker.properties /app/config/

# Add environment variable to help JasperReports find its dependencies
ENV CLASSPATH=/app/jasperreports
ENV SPRING_PROFILES_ACTIVE=docker
ENV SPRING_CONFIG_LOCATION=file:/app/config/

# Add Eureka environment variable explicitly
ENV - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://discovery-server:8761/eureka/

# Expose ports
EXPOSE 8080

# Use simple command
CMD ["java", "-Dspring.profiles.active=docker", "-jar", "notes-application.jar"]