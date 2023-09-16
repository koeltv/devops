## Stage 1: Build the Rust Application
FROM rust:1.72 AS builder
# Copy the Cargo.toml and Cargo.lock files to cache dependencies
COPY Cargo.toml Cargo.lock ./
# Build with only dependencies to cache them
RUN mkdir src && echo 'fn main() {}' > src/main.rs && cargo build --release
# Copy the source code
COPY src ./src
# Build the application
RUN cargo install --path .

## Stage 2: Create a Minimal Container
FROM debian:bookworm-20230904-slim
# Install needed SSL dependency
RUN apt-get update && apt-get install -y --no-install-recommends libssl-dev && rm -rf /var/lib/apt/lists/*
# Copy executable from builder
COPY --from=builder /usr/local/cargo/bin/service1 /service1
# Run the application
ENTRYPOINT ["/service1"]