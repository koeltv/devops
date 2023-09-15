## Stage 1: Build the Kotlin Application
FROM gradle:8.3-jdk17-alpine AS builder
# Copy the source code
COPY --chown=gradle:gradle . /home/gradle/project
# Move to working directory
WORKDIR /home/gradle/project
# Build executable
RUN gradle shadowJar --no-daemon

## Stage 2: Create a Minimal Container
FROM eclipse-temurin:17-jre-alpine
LABEL authors="Valentin Koeltgen"
EXPOSE 8000
# Copy the executable from the builder container
COPY --from=builder /home/gradle/project/build/libs/*.jar service.jar
# Run the executable
ENTRYPOINT ["java", "-jar", "service.jar"]