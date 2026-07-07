FROM maven:3-eclipse-temurin-26 AS build
WORKDIR /app

COPY pom.xml .
COPY media-service/pom.xml media-service/pom.xml

RUN mvn -pl media-service -am dependency:go-offline

COPY media-service/src media-service/src

RUN mvn -pl media-service -am clean package -DskipTests


FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/media-service/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]