#1. build (JDK)
FROM maven:3.8.4-openjdk-17-slim AS build

WORKDIR /app

#2. copy the source code
COPY pom.xml .
COPY src ./src

#3. application build
RUN mvn clean install -DskipTests

#4 run(JDK 17)
FROM openjdk:17-jdk-slim

WORKDIR /app

#5. copy the file built
COPY --from=build /app/target/*.jar app.jar

#6.Spring boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
