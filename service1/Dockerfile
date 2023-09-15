FROM rust:1.72
# Copy the Cargo.toml and Cargo.lock files to cache dependencies
COPY Cargo.toml Cargo.lock ./
# Build the application with dependencies to cache them
RUN mkdir src && echo 'fn main() {}' > src/main.rs && cargo build --release
# Copy the rest of the source code
COPY src ./src
# Build the application
RUN cargo install --path .
ENTRYPOINT ["service1"]
