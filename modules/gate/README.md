# Gateway Service

This is the API Gateway service for the VibeCode microservice architecture. It handles request routing, authentication validation, rate limiting, and cross-cutting concerns.

## Features

- **Path-based Routing**: Routes requests to appropriate microservices based on URL paths
- **JWT Authentication**: Validates JWT tokens for protected routes
- **Rate Limiting**: Prevents abuse with configurable request limits
- **CORS Support**: Enables cross-origin requests for web applications
- **Request Logging**: Comprehensive logging for monitoring and debugging
- **Health Checks**: Built-in health endpoints for service monitoring

## Routes

### Public Routes (No Authentication Required)
- `POST /api/auth/login` → Auth Service (localhost:8081)
- `POST /api/auth/register` → Auth Service (localhost:8081)
- `POST /api/auth/refresh` → Auth Service (localhost:8081)
- `GET /api/gateway/health` → Gateway Health Check
- `GET /api/gateway/routes` → Available Routes Information

### Protected Routes (JWT Authentication Required)
- `GET|POST|PUT|DELETE /api/auth/**` → Auth Service (localhost:8081)
- `GET|POST|PUT|DELETE /api/interview/**` → Interview Service (localhost:8082)

## Configuration

### Environment Variables
- `JWT_SECRET`: Secret key for JWT validation (default: auto-generated)
- `SPRING_PROFILES_ACTIVE`: Active profile (dev, prod)
- `SERVER_PORT`: Gateway port (default: 8080)

### Rate Limiting
- Default: 100 requests per minute per client IP
- Configurable via application properties

## Running the Service

### Prerequisites
1. Java 21 or higher
2. Auth Service running on port 8081
3. Interview Service running on port 8082

### Local Development
```bash
./gradlew bootRun
```

### Docker
```bash
./gradlew bootJar
docker build -t vibecode-gateway .
docker run -p 8080:8080 vibecode-gateway
```

## API Usage Examples

### Login (Public)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"user","password":"pass"}'
```

### Access Protected Resource
```bash
curl -X GET http://localhost:8080/api/interview/questions \
  -H "Authorization: Bearer <your-jwt-token>"
```

### Check Gateway Health
```bash
curl http://localhost:8080/api/gateway/health
```

## Architecture

The gateway service uses Spring Cloud Gateway with reactive programming for high performance and scalability. It includes:

- **Authentication Filter**: Validates JWT tokens and extracts user information
- **Rate Limit Filter**: Implements request throttling per client
- **Logging Filter**: Provides comprehensive request/response logging
- **CORS Filter**: Handles cross-origin resource sharing
- **Global Exception Handler**: Provides consistent error responses

## Security

- JWT tokens are validated for signature and expiration
- User information is extracted and passed to downstream services
- Rate limiting prevents abuse and DDoS attacks
- CORS is configured for secure cross-origin access

## Monitoring

- Health checks available at `/api/gateway/health`
- Actuator endpoints for metrics and monitoring
- Comprehensive logging for request tracing
- Service discovery ready for future scaling
