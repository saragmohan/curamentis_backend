# Build stage
FROM gradle:8.5-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN ./gradlew build -x test

# Package stage
FROM eclipse-temurin:17-jre
VOLUME /tmp
COPY --from=build /home/gradle/src/build/libs/app.jar app.jar
ENTRYPOINT ["java", "-XX:TieredStopAtLevel=1", "-XX:+UseSerialGC", "-Xms128m", "-Xmx384m", "-jar", "/app.jar"]

