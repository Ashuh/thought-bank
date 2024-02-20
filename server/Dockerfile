# Use a base image with the JDK
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the source code into the container
COPY /build/libs/server-all.jar .

# Expose the port your Ktor server listens on
EXPOSE 8080

# Command to compile and run your Ktor server when the container starts
CMD ["java", "-jar", "server-all.jar"]
