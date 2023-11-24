echo "Running service1 unit tests..."
cd ./service1 && cargo test && cd ..
echo "Running service2 unit tests..."
cd ./service2 && chmod +x ./gradlew && ./gradlew test --no-daemon && cd ..
echo "Running monitor unit tests..."
cd ./monitor && chmod +x ./gradlew && ./gradlew test --no-daemon && cd ..
echo "Running gateway tests..."
cd ./gateway && chmod +x ./gradlew && ./gradlew test --no-daemon && cd ..