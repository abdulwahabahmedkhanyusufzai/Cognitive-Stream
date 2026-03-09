# Stage 1: Build
FROM maven:3.9.6-eclipse-temurin-21-jammy AS build
WORKDIR /app

# Optimization: Cache dependencies separately from source code
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and package
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Security: Create a non-privileged user to run the application
RUN addgroup --system spring && adduser --system --group spring
USER spring:spring

# Copy only the compiled artifact from the build stage
COPY --from=build /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Networking
EXPOSE 5000

# Performance: Optimized JVM flags for container environments
ENTRYPOINT ["java", \
            "-XX:+UseContainerSupport", \
            "-XX:MaxRAMPercentage=75.0", \
            "-Djava.security.egd=file:/dev/./urandom", \
            "-jar", \
            "app.jar"]