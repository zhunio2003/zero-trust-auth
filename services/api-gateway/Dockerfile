FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY services/api-gateway/build.gradle services/api-gateway/build.gradle
COPY services/api-gateway/src services/api-gateway/src
COPY services/auth-service/build.gradle services/auth-service/build.gradle
COPY services/authz-service/build.gradle services/authz-service/build.gradle
COPY services/audit-log-service/build.gradle services/audit-log-service/build.gradle
RUN chmod +x gradlew && ./gradlew :services:api-gateway:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/services/api-gateway/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT [ "java", "-jar", "app.jar" ]