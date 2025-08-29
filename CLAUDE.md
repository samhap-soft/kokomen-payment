# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Kokomen Payment (꼬꼬면 결제) is a multi-module Spring Boot payment system implementing Tosspayments integration. The project uses Java 17, Spring Boot 3.4.5, MySQL, Redis, and follows a domain-driven design with layered architecture.

## Module Structure

- **api**: External-facing REST API module with authentication and member services
- **internal**: Internal payment processing module with Tosspayments integration
- **domain**: Core domain entities and repositories (JPA/Hibernate)
- **common**: Shared configurations (Redis, logging)
- **external**: External API clients (Tosspayments)

## Common Commands

### Build & Test
```bash
# Run all tests
./gradlew test

# Build entire project
./gradlew clean build

# Build specific module
./gradlew :api:build
./gradlew :internal:build

# Run tests for specific module
./gradlew :api:test
./gradlew :internal:test
```

### Local Development
```bash
# Start API service locally (includes MySQL & Redis)
cd api && ./run-local-api.sh

# Start Internal service locally  
cd internal && ./run-local-internal.sh

# Start test MySQL container
cd domain && ./run-test-mysql.sh

# Start test Redis container
cd common && ./run-test-redis.sh
```

## Architecture & Key Patterns

### Package Structure
Domain-first organization followed by layered architecture:
```
domain/
├── controller/
├── service/
├── repository/
├── domain/
└── dto/
global/
├── config/
├── exception/
└── infrastructure/
```

### Testing Strategy
- **Integration tests preferred**: Uses real beans with MySQL test containers (not H2)
- **DatabaseCleaner pattern**: Tests use `MySQLDatabaseCleaner` instead of `@Transactional` for proper isolation
- **Base test classes**: `BaseTest` for services, `BaseControllerTest` for controllers with MockMvc
- **Test naming**: Korean method names without `@DisplayName`

### Payment Flow
1. **PaymentFacadeService**: Orchestrates payment confirmation flow
2. **TosspaymentsTransactionService**: Manages transactional operations
3. **TosspaymentsPaymentService**: Handles payment entity operations
4. **TosspaymentsClient**: External API communication with Tosspayments

### Exception Handling
Custom exceptions with HTTP status mapping:
- `BadRequestException` (400)
- `UnauthorizedException` (401)
- `ForbiddenException` (403)

Global exception handler in `GlobalExceptionHandler` class.

### Pagination
- **Always use Spring Data's `Pageable`** for pagination
- Controller methods receive `Pageable` as parameter with `@PageableDefault` annotation
- Example: `@PageableDefault(size = 10) Pageable pageable`
- Service methods accept `Pageable` directly without decomposing into page/size
- Repository methods use `Pageable` for database queries

## Code Style & Conventions

### Java Style
- Based on Woowacourse Java Style Guide (Google Java Style variant)
- **Indentation**: 4 spaces
- **Column limit**: 120 characters (general), 160 (maximum)
- **Line wrapping**: +8 spaces for continuation lines

### Method Parameter Rules
- **Records/Controllers**: One parameter per line
- **Annotated methods or >160 chars**: One per line
- **Regular methods**: No line breaks

### Naming Conventions
- **Methods**: action + domain format (e.g., `saveMember()`)
- **Read vs Find**: `read` throws exception if not found, `find` returns Optional/empty
- **No `get-` prefix** except for actual getters
- **No `not` in validation methods**
- **No `final` in method parameters**

### Lombok Usage
- Use for constructors, getters/setters
- Spring annotations before Lombok annotations
- Domain entities override `toString()`

### DTO-Entity Conversion
- **DTO → Entity**: Conversion methods are placed in DTO classes (e.g., `dto.toEntity()`)
- **Entity → DTO**: Conversion methods are also placed in DTO classes as static factory methods or in service layer
- DTOs are responsible for transformation logic to keep entities clean
- Example: `ConfirmRequest.toTosspaymentsPayment()` in DTO class

### Transaction Management
- **@Transactional always on methods, not classes**
- Place `@Transactional(readOnly = true)` on read-only service methods
- Place `@Transactional` on write service methods
- Never put @Transactional at class level

### Testing Conventions
- Test methods named in Korean
- No `@DisplayName` annotations
- Given-When-Then structure
- Test data initialized in given section (no data.sql)

## Database & Persistence

### Flyway Migrations
Location: `domain/src/main/resources/db/migration/`
Naming: `V{version}__{description}.sql`

### DDL Conventions
- **ENUM columns**: Always use MySQL ENUM type instead of VARCHAR for enum fields
  - Example: `ALTER TABLE table_name ADD COLUMN status ENUM('ACTIVE', 'INACTIVE') NOT NULL`
  - This ensures type safety at the database level and reduces storage size

### Test Containers
MySQL 8.4.5 containers for testing (port 13308)
Configuration in `domain/test.yml`

## Commit Message Convention

Follow Angular commit message convention:
- **feat**: A new feature for the user
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Changes that don't affect code meaning (formatting, etc.)
- **refactor**: Code changes that neither fix bugs nor add features
- **test**: Adding or updating tests
- **chore**: Changes to build process, tools, dependencies

Format: `<type>: <description>`
Example: `feat: 결제 기능 개발`

## Important Notes

1. **No wildcard imports** in Java files
2. **MDC logging context** must be preserved across threads
3. **Security**: Never log or commit secrets/keys
4. **Git commits**: Never commit unless explicitly requested
5. **Documentation**: Don't create README/docs unless requested
6. **Testing**: Always verify with existing test commands before suggesting new ones