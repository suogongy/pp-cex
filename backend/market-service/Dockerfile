# Build stage
FROM maven:3.8.6-openjdk-17 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# Production stage
FROM openjdk:17-jdk-slim
WORKDIR /app

# Create non-root user
RUN groupadd -r spring && useradd -r -g spring spring

# Copy jar file
COPY --from=builder /app/target/*.jar app.jar

# Change ownership
RUN chown -R spring:spring /app
USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8004/market/actuator/health || exit 1

EXPOSE 8004

# JVM optimization for market data service
ENV JAVA_OPTS="-Xms512m -Xmx1024m -Djava.security.egd=file:/dev/./urandom -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseCompressedOops"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]