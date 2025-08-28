# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the Kokomen Payment Service, a Spring Boot multi-module application that handles payment management. The project consists of four main modules:

- **api**: External API server for client-facing payment endpoints
- **internal**: Internal API server for service-to-service payment creation
- **domain**: Core domain entities and repositories (shared by api and internal)
- **common**: Shared configurations and utilities (Redis, logging, exceptions)

## Architecture

### Module Structure
```
kokomen-payment/
├── api/          # External API (port 8080, management: 8001)
├── internal/     # Internal API for service-to-service communication
├── domain/       # JPA entities, repositories, Flyway migrations
└── common/       # Shared utilities, Redis config, exceptions
```

### Technology Stack
- **Java 17** with Spring Boot 3.4.5
- **Spring Data JPA** with MySQL
- **Redis** for session management (API module)
- **Flyway** for database migrations
- **Gradle** multi-module build system
- **Docker Compose** for local development
- **Spring REST Docs** with AsciiDoctor

### Key Domain Concepts
- **Notification**: Core entity with state management (UNREAD/READ)
- **NotificationType**: INTERVIEW_LIKE, ANSWER_LIKE, INTERVIEW_VIEW_COUNT
- **NotificationPayload**: Polymorphic JSON payload storage with custom converter

## Common Development Commands

### Build Commands
```bash
# Build entire project
./gradlew clean build

# Build specific module
./gradlew :api:build
./gradlew :internal:build
./gradlew :domain:build
./gradlew :common:build

# Build without tests
./gradlew build -x test
```

### Test Commands
```bash
# Run all tests
./gradlew test

# Run tests for specific module
./gradlew :api:test
./gradlew :internal:test
./gradlew :domain:test

# Run specific test class
./gradlew test --tests NotificationApiControllerTest
./gradlew test --tests NotificationInternalControllerTest
```

### Local Development
```bash
# Start API module with dependencies
cd api && ./run-local-api.sh

# Start Internal module with dependencies
cd internal && ./run-local-internal.sh

# Start only test databases
cd domain && ./run-test-mysql.sh
cd common && ./run-test-redis.sh
```

### Docker Commands
The project uses Docker Compose for local development with MySQL and Redis:
```bash
# API module local environment
docker compose -f api/local-api-docker-compose.yml up -d

# Internal module local environment
docker compose -f internal/local-internal-docker-compose.yml up -d

# Domain test MySQL
docker compose -f domain/test.yml up -d payment-test-mysql

# Common test Redis
docker compose -f common/test.yml up -d payment-test-redis
```

## Important Files and Patterns

### Authentication
- API module uses `@Authentication` annotation for member authentication
- `MemberAuthArgumentResolver` handles member ID extraction from session
- Redis session management with Spring Session

### Database Migrations
- Flyway migrations located in `domain/src/main/resources/db/migration/`
- Naming convention: `V{version}__{description}.sql`

### Configuration Profiles
- **local**: Local development with CORS for localhost:8080
- **dev**: Development environment with multiple allowed origins
- **prod**: Production environment with strict CORS settings
- **test**: Test configuration for integration tests

### Testing Patterns
- Base test classes: `BaseTest`, `BaseControllerTest`
- Database cleanup: `MySQLDatabaseCleaner` for test isolation
- Fixture builders: `NotificationFixtureBuilder` for test data

### API Documentation
- REST Docs generated during tests
- Documentation available at `/docs` endpoint after build
- Source files in `src/docs/asciidoc/`

## Module Dependencies
```
api → domain, common
internal → domain, common
domain → (standalone, contains entities)
common → (standalone, contains shared utilities)
```

## Port Configuration
- API Server: 8080 (application), 8001 (management/actuator)
- Internal Server: Default Spring Boot port
- Test MySQL: Configured in docker-compose
- Test Redis: Configured in docker-compose
