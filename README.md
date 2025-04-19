# noom-sleep-logger

[![Build Status](https://github.com/yegor-usoltsev/noom-sleep-logger/actions/workflows/ci.yml/badge.svg)](https://github.com/yegor-usoltsev/noom-sleep-logger/actions)
[![Codecov](https://codecov.io/github/yegor-usoltsev/noom-sleep-logger/graph/badge.svg?token=REDMRSKCPX)](https://codecov.io/github/yegor-usoltsev/noom-sleep-logger)

> [!NOTE]
> You can find the original project instructions in [ASSIGNMENT.md](ASSIGNMENT.md).

A RESTful API service for tracking sleep patterns.

## Prerequisites

- Java 21
- Gradle
- Docker and Docker Compose

## Getting Started

1. Start the PostgreSQL database:

```bash
docker compose up -d
```

2. Build the project (requires PostgreSQL to be running):

```bash
./gradlew build
```

3. Run the application:

   You can run the application in one of two ways:

   a. Using Gradle:

   ```bash
   ./gradlew bootRun
   ```

   b. Using Docker (requires the project to be built first):

   ```bash
   docker compose --profile app up -d --build
   ```

   Alternatively, you can use the [pre-built Docker image](https://github.com/users/yegor-usoltsev/packages/container/package/noom-sleep-logger):

   ```bash
   docker pull ghcr.io/yegor-usoltsev/noom-sleep-logger:latest
   ```

   The application will be available at `http://localhost:8080`.

## API Documentation

A complete Postman collection is available in the repository ([`postman_collection.json`](postman_collection.json)). You can also [access the online version](https://yegor-9968632.postman.co/workspace/Yegor's-Workspace~bf696e4d-866e-4299-8a61-94e788dae748/collection/44197967-00378ea9-587f-4e8a-bb94-0f33304f7369?action=share&creator=44197967) of the collection.
