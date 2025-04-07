FROM maven:3-eclipse-temurin-21 AS build

LABEL Application="Notification Service"

WORKDIR /src

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests -e


FROM openjdk:21

WORKDIR /src

COPY --from=build /src/target/*.jar /src/notificationService.jar

EXPOSE 8081

HEALTHCHECK --interval=30s --timeout=3s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

CMD ["java", "-jar", "notificationService.jar"]