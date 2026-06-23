#!/bin/bash
# Initialize Gradle wrapper for the project

echo "Initializing Gradle wrapper..."
gradle wrapper --gradle-version 8.7

echo "Making gradlew executable..."
chmod +x gradlew

echo "Gradle wrapper initialized successfully!"
echo "You can now run: ./gradlew bootRun"
