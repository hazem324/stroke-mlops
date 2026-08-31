# Stage 1 - Build
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

# Copy Maven Wrapper
COPY mvnw ./
RUN chmod +x mvnw

# Copy Maven configuration
COPY .mvn .mvn
COPY pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build Spring Boot JAR
RUN ./mvnw clean package -DskipTests

# Stage 2 - Runtime
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR
COPY --from=builder /app/target/*.jar app.jar

# Set ownership
RUN chown spring:spring app.jar

# Run as non-root user
USER spring

EXPOSE 8002

ENTRYPOINT ["java", "-jar", "app.jar"]