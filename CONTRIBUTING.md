# Contributing to SAMAP

Thank you for your interest in contributing to SAMAP! This document provides guidelines and information for contributors.

## 🚀 Getting Started

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Maven 3.8+
- Git

### Development Setup

1. **Fork the repository**
   ```bash
   git clone https://github.com/your-username/samap.git
   cd samap
   ```

2. **Set up development environment**
   ```bash
   # Install dependencies
   mvn clean install
   
   # Set up database
   createdb samap_db
   ```

3. **Run tests**
   ```bash
   mvn test
   ```

4. **Start development server**
   ```bash
   mvn spring-boot:run
   ```

## 📝 Code Style

### Java Code Style
- Follow standard Java naming conventions
- Use meaningful variable and method names
- Add JavaDoc comments for public methods
- Keep methods focused and small
- Use Lombok annotations to reduce boilerplate

### Example:
```java
/**
 * Authenticates user and generates JWT tokens
 * @param loginRequest User credentials
 * @return Authentication response with tokens
 */
@Transactional
public AuthenticationResponse authenticate(LoginRequest loginRequest) {
    // Implementation
}
```

### Database Conventions
- Use snake_case for table and column names
- Include created_at and updated_at timestamps
- Use meaningful constraint names
- Add proper indexes for performance

## 🔒 Security Guidelines

### Authentication & Authorization
- Always validate user permissions
- Use parameterized queries to prevent SQL injection
- Implement proper error handling without exposing sensitive information
- Follow OWASP security guidelines

### Data Protection
- Never log sensitive information (passwords, tokens)
- Use BCrypt for password hashing
- Implement proper session management
- Validate all input data

## 🧪 Testing

### Unit Tests
- Write tests for all business logic
- Use meaningful test names
- Follow AAA pattern (Arrange, Act, Assert)
- Mock external dependencies

### Integration Tests
- Test API endpoints
- Test database interactions
- Test security configurations

### Example Test:
```java
@Test
@DisplayName("Should authenticate user with valid credentials")
void shouldAuthenticateUserWithValidCredentials() {
    // Arrange
    LoginRequest request = new LoginRequest("admin", "admin123");
    
    // Act
    AuthenticationResponse response = authService.authenticate(request);
    
    // Assert
    assertThat(response.getAccessToken()).isNotNull();
    assertThat(response.getUser().getUsername()).isEqualTo("admin");
}
```

## 📋 Pull Request Process

1. **Create a feature branch**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write clean, well-documented code
   - Add tests for new functionality
   - Update documentation if needed

3. **Test your changes**
   ```bash
   mvn clean test
   mvn spring-boot:run
   ```

4. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add user role management feature"
   ```

5. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

6. **Create a Pull Request**
   - Provide a clear description of changes
   - Reference any related issues
   - Include screenshots for UI changes

## 🐛 Bug Reports

When reporting bugs, please include:
- **Description**: Clear description of the issue
- **Steps to Reproduce**: Detailed steps to reproduce the bug
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Environment**: OS, Java version, browser (if applicable)
- **Screenshots**: If applicable

## 💡 Feature Requests

For feature requests, please include:
- **Problem Statement**: What problem does this solve?
- **Proposed Solution**: How should it work?
- **Alternatives**: Any alternative solutions considered
- **Additional Context**: Any other relevant information

## 📚 Documentation

- Update README.md for significant changes
- Add JavaDoc comments for public APIs
- Update API documentation for endpoint changes
- Include code examples where helpful

## 🏷️ Commit Message Guidelines

Use conventional commit format:
- `feat:` New feature
- `fix:` Bug fix
- `docs:` Documentation changes
- `style:` Code style changes
- `refactor:` Code refactoring
- `test:` Adding or updating tests
- `chore:` Maintenance tasks

Examples:
```
feat: add user role management API
fix: resolve JWT token expiration issue
docs: update API documentation
test: add integration tests for auth service
```

## 🤝 Code Review Guidelines

### For Reviewers
- Be constructive and respectful
- Focus on code quality and security
- Check for proper testing
- Verify documentation updates

### For Contributors
- Respond to feedback promptly
- Make requested changes
- Keep discussions focused on the code

## 📞 Getting Help

- **Issues**: Create a GitHub issue for bugs or questions
- **Discussions**: Use GitHub Discussions for general questions
- **Email**: Contact the maintainer for sensitive issues

## 📄 License

By contributing to SAMAP, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to SAMAP! 🛡️
