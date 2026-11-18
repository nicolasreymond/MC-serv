# Build stage
FROM docker.io/library/maven:3.9-eclipse-temurin-25 AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

# Runtime stage
FROM docker.io/library/eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /build/target/MC-serv.jar ./MC-serv.jar
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "MC-serv.jar"]
CMD ["server"]
