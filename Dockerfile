#Maven with Eclipse Temurin JDK 21
FROM maven:3-eclipse-temurin-21 AS build

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

EXPOSE 8080

CMD ["java", "-jar", "notes-application.jar"]