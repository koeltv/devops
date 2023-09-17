# Stage 1: Build the Kotlin Application
FROM eclipse-temurin:17 AS builder
# Move to working directory
WORKDIR /home/gradle/project
# Copy the source code, config files and gradle wrapper
COPY . .
# Define environment variable
ENV JAVA_HOME=/opt/java/openjdk
# Build custom JRE
RUN ["./gradlew", "jre", "--no-daemon"]
# Build executable
RUN ["./gradlew", "shadowJar", "--no-daemon"]

## Stage 2: Create a Minimal Container
FROM bellsoft/alpaquita-linux-base
LABEL authors="Valentin Koeltgen"
EXPOSE 8000
# Copy the custom JRE from the builder container
COPY --from=builder /home/gradle/project/build/jre/service2-alpine-linux /jre
# Copy the executable from the builder container
COPY --from=builder /home/gradle/project/build/libs/*.jar service.jar
# Run the executable
ENTRYPOINT ["/jre/bin/java", "-jar", "service.jar"]