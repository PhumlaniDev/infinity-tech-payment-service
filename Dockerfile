# First stage: Build the JAR
FROM eclipse-temurin:21-jdk-jammy AS builder
WORKDIR /app
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline

COPY src ./src
RUN ./mvnw package -DskipTests

# Second stage: Minimal runtime image
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/payment-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "/app/app.jar"]