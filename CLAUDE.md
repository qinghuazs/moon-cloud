# Claude Code Development Guide for Moon Cloud

This document provides comprehensive guidance for future Claude Code instances to understand and work effectively with the Moon Cloud microservices framework.

## 回答问题时用中文

## 在处理复杂问题时进行深度思考

## 在开发复杂功能时遵循 需求分析-功能拆解-流程设计-系统设计-编码 的流程，先输出需求文档，再输出详细设计文档，最后再进行开发

## 开发完成后编写单元测试文档

## 如果涉及到对外接口，编写或者更新对应模块的接口文档

## 使用 Spring Boot 3.4.1 的版本，不要使用已废弃的特性

## Project Overview

Moon Cloud is a Java microservices framework built on Spring Boot 3.4.1 and Java 21, designed to provide a scalable and modular platform for business applications.

### Key Technologies
- **Java**: 21
- **Spring Boot**: 3.4.1
- **Spring Cloud**: 2023.0.0
- **Spring Cloud Alibaba**: 2023.0.0.0-RC1
- **MyBatis Plus**: 3.5.12
- **MySQL**: 8.0.33
- **Redis**: With Lettuce client
- **Maven**: Multi-module project structure

## Project Structure

The project follows a multi-module Maven architecture:

```
moon-cloud/
├── moon-dependencies/        # Dependency management (BOM)
├── moon-framework/          # Core framework components
│   ├── moon-spring-boot-starter-response/
│   ├── moon-spring-boot-starter-threadpool/
│   └── moon-spring-boot-starter-validator/
├── moon-business/           # Business modules
│   ├── moon-business-user/     # User management system
│   ├── moon-business-shorturl/ # URL shortening service
│   ├── moon-business-gps/      # GPS tracking service
│   ├── moon-business-email/    # Email service
│   ├── moon-business-eureka/   # Service discovery
│   ├── moon-business-gateway/  # API gateway
│   └── moon-business-web-drift-bottle/
├── moon-ai/                 # AI/ML related modules
└── moon-java/              # Java utilities and algorithms
```

## Build Commands

### Core Maven Commands
```bash
# Clean and install all modules
mvn clean install

# Compile specific module
mvn clean compile -pl module-name

# Run specific business module
mvn spring-boot:run -pl moon-business/moon-business-modulename
```

### Environment Setup
Always use environment variables for configuration. Example for moon-business-user:

```bash
# Basic startup

# Full configuration startup
```

## Moon Business User Module

The user management system is the most active module, featuring:

### Key Features
- **RBAC Authentication**: Role-Based Access Control with JWT tokens
- **User Management**: Registration, login, profile management
- **Session Management**: Redis-based distributed sessions
- **Excel Export**: User data export functionality
- **Google OAuth**: Third-party authentication support

### Configuration Pattern
This module demonstrates the recommended configuration approach:

1. **Environment Variables**: All sensitive configuration (DB, Redis, JWT secrets) via .env files
2. **Java Configuration**: Database and Redis configured via Java classes, not YAML
3. **Security**: No hardcoded credentials in source code

### Important Files
- **Configuration**: `src/main/java/com/moon/cloud/user/config/`
  - `DatabaseConfig.java`: HikariCP database configuration
  - `RedisConfig.java`: Lettuce Redis configuration with connection pooling
- **Environment**: `.env` file with all configuration variables
- **Documentation**: `REDIS-DOCKER-GUIDE.md` for Redis deployment

### Startup Process
1. Ensure .env file exists with correct configuration
2. Use full environment variable command (see above)
3. Application starts on http://localhost:8080/api/user
4. Swagger UI available at `/swagger-ui/index.html`

## Common Issues and Solutions

### Maven Build Issues
1. **Spring Boot Plugin Version**: Always specify version 3.4.1 explicitly
2. **Dependency Conflicts**: Use dependency exclusions in pom.xml
3. **Property Resolution**: Use explicit versions instead of property placeholders when needed

### Configuration Issues
1. **Bean Conflicts**: Remove duplicate configuration classes
2. **Database Access**: Ensure environment variables match database permissions
3. **Redis Connection**: Verify Redis server accessibility and password

### Spring Boot Issues
1. **Port Conflicts**: Check if port 8080 is available
2. **Context Path**: Applications may have custom context paths like `/api/user`
3. **Profile Management**: Use appropriate Spring profiles for different environments

## Development Patterns

### Code Standards
- **Lombok**: Used extensively for reducing boilerplate
- **Validation**: JSR-303 validation annotations
- **REST APIs**: Follow RESTful principles
- **Exception Handling**: Centralized exception management
- **Response Format**: Standardized response format via custom starter

### Database Patterns
- **MyBatis Plus**: ORM with automatic CRUD operations
- **Connection Pooling**: HikariCP for production-ready connections
- **Migration**: Database schema managed separately

### Security Patterns
- **JWT Tokens**: Stateless authentication
- **Redis Sessions**: Distributed session management
- **Environment Variables**: Configuration security
- **OAuth Integration**: Third-party authentication support

## Testing and Quality

### Running Tests
```bash
# Run all tests
mvn test

# Run tests for specific module
mvn test -pl moon-business/moon-business-user

# Skip tests during build
mvn clean install -DskipTests
```

### Code Quality
- **Spring Boot Actuator**: Health checks and metrics
- **Swagger Documentation**: API documentation generation
- **Logging**: SLF4J with Logback
- **Monitoring**: Micrometer with Prometheus support

## Deployment Considerations

### Docker Support
- Redis deployment documented in REDIS-DOCKER-GUIDE.md
- Environment variable-based configuration supports containerization
- Health checks available via Spring Boot Actuator

### Production Setup
1. **Database**: Use connection pooling with appropriate pool sizes
2. **Redis**: Configure connection pooling and timeout settings
3. **Security**: Use strong JWT secrets and proper password policies
4. **Monitoring**: Enable Actuator endpoints for production monitoring

## Module-Specific Notes

### moon-business-user
- **Port**: 8080 with context path `/api/user`
- **Database**: Requires MySQL with specific schema
- **Redis**: Required for session management
- **Configuration**: Uses .env file pattern

### Other Modules
- **moon-business-shorturl**: URL shortening service
- **moon-business-gps**: GPS tracking functionality
- **moon-business-email**: Email service integration
- **moon-business-eureka**: Service discovery server

## Repository Information

- **Maven Repository**: Uses Aliyun mirror for faster downloads in China
- **Version Management**: Uses revision property for unified versioning
- **Dependency Management**: Centralized in moon-dependencies module

## Quick Start Checklist

When working with this codebase:

1. ✅ Check Java 21 is installed
2. ✅ Verify Maven 3.6+ is available
3. ✅ Review .env files for required configuration
4. ✅ Run `mvn clean install` from root to build all modules
5. ✅ Use environment variables for sensitive configuration
6. ✅ Start with moon-business-user as the reference implementation
7. ✅ Check Spring Boot Actuator endpoints for health status
8. ✅ Use Swagger UI for API exploration and testing

## Best Practices for Claude Code

1. **Always use environment variables** for database and Redis configuration
2. **Follow the Java configuration pattern** established in moon-business-user
3. **Use the TodoWrite tool** for tracking multi-step development tasks
4. **Verify builds** with `mvn clean compile` before running applications
5. **Check for existing patterns** in moon-business-user before creating new modules
6. **Use the standardized response format** from moon-spring-boot-starter-response
7. **Follow the security patterns** established for JWT and Redis sessions

This guide should enable future Claude Code instances to quickly understand and work effectively with the Moon Cloud codebase.