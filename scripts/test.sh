#!/bin/bash

# A script to compare the Fat jar vs The Shaky jar sizes and launch them both to see if they work like expected

# Print the size of a JAR file in megabytes
print_jar_size() {
    local jar_path=$1
    local jar_size=$(stat -c %s "$jar_path")
    local jar_size_mb=$((jar_size / (1024 * 1024)))
    local jar_size_decimal=$(( (jar_size % (1024 * 1024)) * 100 / (1024 * 1024) ))
    echo "Size of $jar_path: $jar_size_mb.$jar_size_decimal MB"
}

echo "Building the project..."
./gradlew build &
wait

echo ""

jar1="./build/libs/minecraft-sync.jar"
print_jar_size "$jar1"
echo ""
echo "Running Fat Jar:"
java -jar "$jar1"

echo ""

jar2="./build/libs/minecraft-sync-shaky.jar"
print_jar_size "$jar2"
echo ""
echo "Running Shaky Jar:"
java -jar "$jar2"
