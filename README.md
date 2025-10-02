# VibeCode

A comprehensive full-stack microservices application for coding interviews and assessments, built with Java Spring Boot and modern technologies. VibeCode provides a complete platform for conducting technical interviews, managing coding challenges, and evaluating submissions with AI assistance.

## 🏗️ Architecture Overview

VibeCode follows a microservices architecture with the following key components:

- **API Gateway** (`gate`) - Central routing and authentication hub
- **Authentication Service** (`auth`) - User management and security
- **Interview Service** (`interview`) - Interview scheduling and management  
- **Submission Service** (`submission`) - Code submission processing
- **AI Helper Service** (`aiHelper`) - AI-powered code assistance
- **Coding Service** (`coding`) - Programming challenge management

## 📁 Project Structure

```
vibecode/
├── build.gradle                 # Root build configuration
├── settings.gradle             # Multi-module project settings
├── gradlew, gradlew.bat       # Gradle wrapper scripts
├── init.sql                   # Database initialization script
├── HELP.md                    # Spring Boot help documentation
├── modules/                   # Microservice modules
│   ├── gate/                  # API Gateway (Java 21 + Spring Cloud Gateway)
│   │   ├── build.gradle.kts   # Kotlin DSL build file
│   │   ├── Dockerfile         # Container configuration
│   │   └── README.md          # Gateway-specific documentation
│   ├── auth/                  # Authentication Service (Java 21 + Spring Boot)
│   ├── interview/             # Interview Management Service
│   ├── submission/            # Code Submission Processing
│   ├── aiHelper/              # AI Assistant Service
│   └── coding/                # Coding Challenge Management
└── build/                     # Build artifacts and resources
```

## 🔧 Module Details

### 🚪 API Gateway (`gate`)
**Technology**: Java 21 + Spring Cloud Gateway + Kotlin DSL
**Port**: 8080 (default)

**Purpose**:
- Central entry point for all client requests
- JWT-based authentication validation
- Request routing to microservices
- Rate limiting and CORS handling
- Health monitoring and logging

**Key Features**:
- Path-based routing to backend services
- Public routes for authentication endpoints
- Protected routes requiring JWT tokens
- Dockerized deployment support
- Built-in health checks

**Routes**:
- Public: `/api/auth/login`, `/api/auth/register`, `/api/auth/refresh`
- Protected: `/api/auth/**`, `/api/interview/**`
- Health: `/api/gateway/health`, `/api/gateway/routes`

### 🔐 Authentication Service (`auth`)
**Technology**: Java 21 + Spring Boot + Spring Security
**Port**: 8081

**Purpose**:
- User registration and authentication
- JWT token generation and validation
- Role-based access control (student, admin, interviewer)
- Password management and security

**Key Features**:
- Secure user registration and login
- JWT token-based authentication
- Role-based authorization
- Password encryption
- User profile management

### 🎤 Interview Service (`interview`)
**Technology**: Java 21 + Spring Boot
**Port**: 8082

**Purpose**:
- Interview session scheduling and management
- Real-time interview collaboration
- Interview recording and feedback
- Candidate-interviewer matching

**Key Features**:
- Interview lifecycle management
- Real-time communication support
- Performance tracking
- Feedback collection system

### 📝 Submission Service (`submission`)
**Technology**: Java 21 + Spring Boot

**Purpose**:
- Code submission processing and evaluation
- Automated testing and scoring
- Performance benchmarking
- Result analytics

**Key Features**:
- Multi-language code execution
- Automated test case validation
- Performance metrics collection
- Plagiarism detection capabilities

### 🤖 AI Helper Service (`aiHelper`)
**Technology**: Java 21 + Spring Boot + AI Integration

**Purpose**:
- Intelligent code analysis and suggestions
- Automated code review and feedback
- Hint generation for coding problems
- Performance optimization recommendations

**Key Features**:
- Real-time code assistance
- Code quality analysis
- Learning pattern recognition
- Intelligent hint system

### 💻 Coding Service (`coding`)
**Technology**: Java 21 + Spring Boot + JPA

**Purpose**:
- Programming challenge creation and management
- Test case management and validation
- Difficulty categorization
- Challenge search and filtering

**Key Features**:
- CRUD operations for coding problems
- Multiple programming language support
- Automated test case execution
- Challenge difficulty algorithms

## 🗄️ Database Schema

The project uses MySQL with a comprehensive schema (`videcode`) that includes:

- **Users**: User profiles with roles (student, admin, interviewer)
- **Interviews**: Interview sessions and scheduling
- **Submissions**: Code submissions and evaluations
- **Challenges**: Programming problems and test cases
- **Scores**: Performance tracking and analytics

Key features:
- UUID-based primary keys
- Proper foreign key relationships
- Audit timestamps (created_at, updated_at)
- Role-based access control
- Performance optimization indexes

## 🚀 Installation & Setup

### Prerequisites

- **Java 21** (ensure `JAVA_HOME` is set)
- **MySQL 8.0+** or **PostgreSQL 13+**
- **Git** for version control
- **Docker** (optional, for containerized deployment)
- **IntelliJ IDEA** (recommended IDE)

### Quick Start

1. **Clone the Repository**:
   ```bash
   git clone <repository-url>
   cd vibecode
   ```

2. **Database Setup**:
   ```bash
   # Create MySQL database
   mysql -u root -p
   CREATE DATABASE videcode;
   
   # Run initialization script
   mysql -u root -p videcode < init.sql
   ```

3. **Configure Database Connection**:
   Update `build/resources/main/application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/videcode
   spring.datasource.username=kumar
   spring.datasource.password=kumar
   spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
   ```

4. **Build All Modules**:
   ```bash
   # Windows
   .\gradlew.bat clean build
   
   # Linux/Mac
   ./gradlew clean build
   ```

5. **Start Services** (in order):
   ```bash
   # 1. Start API Gateway first
   cd modules/gate
   .\gradlew.bat bootRun
   
   # 2. Start Authentication Service
   cd modules/auth
   .\gradlew.bat bootRun
   
   # 3. Start other services as needed
   cd modules/interview
   .\gradlew.bat bootRun
   ```

### Development Setup in IntelliJ IDEA

1. **Import Project**:
   - File → Open → Select `vibecode` directory
   - Choose "Import as Gradle Project"
   - Wait for project indexing to complete

2. **Configure Project SDK**:
   - File → Project Structure → Project SDK → Java 21

3. **Run Configurations**:
   - Create Spring Boot run configurations for each module
   - Set main class and working directory appropriately
   - Configure environment variables and VM options

## 🐳 Docker Deployment

### Gateway Service (Dockerized)
```bash
cd modules/gate
docker build -t vibecode-gateway .
docker run -p 8080:8080 vibecode-gateway
```

### Full Stack Deployment
Create `docker-compose.yml`:
```yaml
version: '3.8'
services:
  database:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: videcode
      MYSQL_USER: YOUR_USERNAME
      MYSQL_PASSWORD: YOUR_PASSWORD
      MYSQL_ROOT_PASSWORD: YOUR_ROOT_PASSWORD
    ports:
      - "3306:3306"
    volumes:
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql

  gateway:
    build: ./modules/gate
    ports:
      - "8080:8080"
    depends_on:
      - database
    environment:
      - SPRING_PROFILES_ACTIVE=docker

  auth:
    build: ./modules/auth
    ports:
      - "8081:8081"
    depends_on:
      - database
      - gateway
    environment:
      - SPRING_DATASOURCE_URL=jdbc:mysql://database:3306/videcode
      - SPRING_DATASOURCE_USERNAME=YOUR_USERNAME
      - SPRING_DATASOURCE_PASSWORD=YOUR_PASSWORD
```

## 🔄 Application Workflow

### 1. User Authentication Flow
```
Client → API Gateway → Auth Service → Database
                    ↓
                JWT Token Generated
                    ↓
            Stored in Client (localStorage/cookie)
```

### 2. Interview Process Flow
```
Client → API Gateway → Interview Service → Database
           ↓              ↓
    JWT Validation → Real-time Communication
           ↓              ↓
    Coding Service → AI Helper Service
           ↓              ↓
    Submission Service → Evaluation Results
```

### 3. Code Submission Flow
```
Client → API Gateway → Submission Service → Execution Environment
           ↓              ↓                      ↓
    Authentication → Database Storage → Test Case Validation
           ↓              ↓                      ↓
    AI Helper → Performance Analysis → Results & Feedback
```

## 🧪 Testing

### Run All Tests
```bash
# Root level - runs tests for all modules
.\gradlew.bat test

# Generate test reports
.\gradlew.bat test jacocoTestReport
```

### Module-Specific Testing
```bash
# Test individual modules
cd modules/auth
.\gradlew.bat test

cd modules/gate
.\gradlew.bat test
```

### Test Reports
- HTML reports: `build/reports/tests/test/index.html`
- Coverage reports: `build/reports/jacoco/test/html/index.html`

## 🤝 Contributing

### Development Workflow

1. **Fork and Clone**:
   ```bash
   git clone <your-fork-url>
   cd vibecode
   git checkout -b feature/your-feature-name
   ```

2. **Before Development**:
   ```bash
   .\gradlew.bat clean build
   .\gradlew.bat test
   ```

3. **Code Standards**:
   - Follow Google Java Style Guide
   - Use meaningful commit messages
   - Write comprehensive tests
   - Document public APIs
   - Add proper logging

4. **Pull Request Process**:
   - Ensure all tests pass
   - Update documentation if needed
   - Add integration tests for new features
   - Request review from maintainers

### Module-Specific Guidelines

#### Adding New Features
- **API Gateway**: Update routing configuration and security rules
- **Authentication**: Implement proper security measures and JWT handling
- **Services**: Follow Spring Boot best practices and RESTful design
- **Database**: Update `init.sql` and provide migration scripts

#### Best Practices
- Use dependency injection properly
- Implement comprehensive error handling
- Add structured logging with appropriate levels
- Write both unit and integration tests
- Follow microservices communication patterns

## 🔧 Configuration

### Environment Variables
```bash
# Database Configuration
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/videcode
SPRING_DATASOURCE_USERNAME=YOUR_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_USERNAME

# JWT Configuration
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION=86400000

# Service Ports
GATEWAY_PORT=8080
AUTH_PORT=8081
INTERVIEW_PORT=8082

# Profiles
SPRING_PROFILES_ACTIVE=dev
```

### Production Configuration
- Use externalized configuration
- Set up proper logging levels
- Configure monitoring and health checks
- Enable security features (HTTPS, CSRF protection)
- Set up database connection pooling

## 🚨 Troubleshooting

### Common Issues

1. **Build Failures**:
   ```bash
   .\gradlew.bat clean build --refresh-dependencies
   .\gradlew.bat --stop
   ```

2. **Database Connection Issues**:
   - Verify MySQL service is running
   - Check connection parameters in application.properties
   - Ensure database and tables exist (run init.sql)
   - Verify user permissions

3. **Port Conflicts**:
   - Check if ports 8080, 8081, 8082 are available
   - Modify server.port in application.properties if needed
   - Use `netstat -an | findstr :8080` to check port usage

4. **Module Startup Order**:
   - Always start API Gateway first (port 8080)
   - Start Authentication service second (port 8081)
   - Other services can start in any order

5. **JWT Token Issues**:
   - Verify JWT_SECRET is consistent across services
   - Check token expiration settings
   - Validate token format and claims

## 📊 Monitoring & Performance

### Health Checks
- Gateway: `http://localhost:8080/api/gateway/health`
- Services: `http://localhost:808x/actuator/health`

### Key Metrics
- API response times
- Database query performance
- Code execution times
- Memory and CPU usage
- Authentication success rates

### Logging
- Structured logging with JSON format
- Distributed tracing support
- Error tracking and alerting
- Performance monitoring

## 🔒 Security Features

- **Authentication**: JWT-based token authentication
- **Authorization**: Role-based access control (RBAC)
- **API Security**: Rate limiting and request validation
- **Data Protection**: Input sanitization and SQL injection prevention
- **Code Execution**: Sandboxed environment for code submissions
- **HTTPS**: SSL/TLS encryption in production

## 📈 Future Enhancements

- Real-time collaborative coding features
- Advanced AI-powered code suggestions
- Mobile application support
- Integration with popular IDEs
- Advanced analytics dashboard
- Automated interview scheduling
- Video conferencing integration
- Multi-language support

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙋‍♂️ Support & Contact

For questions, issues, or contributions:
- **Issues**: Create GitHub issues for bugs or feature requests
- **Discussions**: Join our community discussions
- **Email**: Contact project maintainers
- **Documentation**: Check module-specific README files

## 🚀 Getting Help

1. **Documentation**: Start with this README and module-specific docs
2. **Issues**: Search existing issues before creating new ones
3. **Community**: Join our development community
4. **Code Review**: All contributions go through code review process

---

**Built with ❤️ for the coding community** 

*Last updated: October 2025*
