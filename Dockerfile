#1. build (JDK)
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

#2. copy the source code
COPY diexample/pom.xml .
COPY diexample/src ./src

#3. application build
RUN mvn clean install -DskipTests

#4 run(JDK 17)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

#5. copy the file built
COPY --from=build /app/target/*.jar app.jar

#6.Spring boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
