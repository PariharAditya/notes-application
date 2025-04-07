FROM maven:3-eclipse-temurin-21 AS build

LABEL application="Discovery Server"

WORKDIR /src

# Copy pom.xml first (for better caching)
COPY pom.xml .

RUN mvn dependency:go-offline

# Copy the whole src directory
COPY src ./src

# Debug: See what we're working with
RUN ls -la && \
    # Run Maven build with explicit output to target directory
    mvn clean package -DskipTests -e && \
    # Debug: Verify the JAR was created
    ls -la target/

# Second stage: runtime
FROM openjdk:21

WORKDIR /app

# Make sure file names match exactly!
COPY --from=build /src/target/*.jar /app/discoveryServer.jar

EXPOSE 8761

HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Use the correct JAR name and path
CMD ["java", "-jar", "/app/discoveryServer.jar"]