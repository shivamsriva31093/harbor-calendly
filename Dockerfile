# Use JDK 17 for building
FROM gradle:8.5-jdk17 AS builder

# Copy local code to the container
WORKDIR /app
COPY . .

# Build application
RUN gradle build --no-daemon

# Use JRE for runtime
FROM eclipse-temurin:17-jre-alpine

# Copy the built artifact from builder
COPY --from=builder /app/build/libs/*-fat.jar app.jar

# Set environment variables
ENV VERTX_PROFILE=prod
ENV PORT=8080

# Create a non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Expose the application port
EXPOSE 8080

# Run the application
CMD ["java", \
     "-XX:+UseContainerSupport", \
     "-XX:MaxRAMPercentage=75.0", \
     "-jar", \
     "app.jar", "--conf", "config/app.config.json"]
