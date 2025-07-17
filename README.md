# 🛡️ SAMAP - Secure Access Management & Audit Platform

> **Enterprise-grade cybersecurity platform with AI-powered threat detection and comprehensive audit capabilities**

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14+-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-success.svg)](https://github.com/your-username/samap)
[![Security](https://img.shields.io/badge/Security-Enterprise%20Grade-red.svg)](https://github.com/your-username/samap)

---

## 🚀 Overview

**SAMAP** is a comprehensive cybersecurity platform designed for enterprise-level security monitoring, user access management, and compliance auditing. Built with modern Java technologies and security best practices, it provides real-time threat detection, comprehensive audit logging, and role-based access control with AI-powered anomaly detection.

### ✨ Key Features

- **🔐 Advanced Authentication** - JWT-based authentication with compact tokens and refresh mechanism
- **👥 User Management** - Complete user lifecycle with role-based access control (RBAC)
- **📊 Security Monitoring** - Real-time threat detection and risk assessment with AI analytics
- **📋 Audit Logging** - Comprehensive activity tracking and compliance reporting
- **🤖 AI-Powered Analytics** - Behavioral analysis and anomaly detection for login patterns
- **☁️ Cloud Ready** - AWS integration ready (S3, Lambda, SNS, KMS)
- **🔒 Enterprise Security** - SOX, HIPAA, GDPR compliance features
- **📱 Professional UI** - Modern responsive web interface with demo guide
- **🔧 Developer Friendly** - Comprehensive API documentation and Postman collections

## 🛠️ Technology Stack

### Backend
- **Java 17** - Modern Java with latest features
- **Spring Boot 3.2.0** - Enterprise application framework
- **Spring Security** - Comprehensive security framework
- **Spring Data JPA** - Data persistence layer
- **PostgreSQL** - Production-grade database
- **JWT** - Stateless authentication with compact tokens
- **Lombok** - Reduced boilerplate code

### Frontend
- **HTML5/CSS3/JavaScript** - Modern web standards
- **Bootstrap** - Responsive design framework
- **Chart.js** - Data visualization
- **Font Awesome** - Professional icons

### DevOps & Cloud
- **Docker** - Containerization ready
- **GitHub Actions** - CI/CD pipeline ready
- **AWS Services** - Cloud integration ready
- **Maven** - Build automation

---

## 🚀 Quick Start

### Prerequisites
- Java 17+
- PostgreSQL 14+
- Maven 3.8+

### Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/your-username/samap.git
   cd samap
   ```

2. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE samap_db;
   CREATE USER samap_user WITH PASSWORD 'your_password';
   GRANT ALL PRIVILEGES ON DATABASE samap_db TO samap_user;
   ```

3. **Configure application properties**
   ```bash
   # Update src/main/resources/application.yml with your database credentials
   ```

4. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

5. **Access the application**
   - **Web Interface:** http://localhost:8080
   - **Demo Guide:** http://localhost:8080/demo-guide.html
   - **Health Check:** http://localhost:8080/actuator/health

### Demo Accounts

| User | Username | Password | Role | Access Level |
|------|----------|----------|------|--------------|
| **System Administrator** | `admin` | `admin123` | Admin | Full System Access |
| **Demo User** | `demo` | `demo123` | User | Standard Access |
| **Neeraj Kumar** | `neeraj` | `password` | User | Standard Access |

---

## 📚 API Documentation

### Authentication
```bash
# Login
POST /api/auth/login
Content-Type: application/json
{
  "username": "admin",
  "password": "admin123"
}

# Response includes compact JWT token (~65% smaller than standard)
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "user": { ... },
  "riskScore": 0.05
}
```

### Key Endpoints
- `GET /api/users` - List all users
- `POST /api/users` - Create new user
- `GET /api/audit/logs` - Get audit logs
- `GET /api/security/status` - Security monitoring status
- `GET /api/reports/compliance` - Compliance reports

**Authorization Header:** `Bearer YOUR_JWT_TOKEN`

---

## 🏗️ Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Frontend      │    │   Backend       │    │   Database      │
│   (HTML/JS)     │◄──►│   (Spring Boot) │◄──►│   (PostgreSQL)  │
│                 │    │                 │    │                 │
│ • Login UI      │    │ • JWT Auth      │    │ • User Data     │
│ • Dashboard     │    │ • REST APIs     │    │ • Audit Logs    │
│ • Demo Guide    │    │ • Security      │    │ • Roles/Perms   │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                │
                       ┌─────────────────┐
                       │   AI Service    │
                       │   (Python)      │
                       │                 │
                       │ • Anomaly Det.  │
                       │ • Risk Scoring  │
                       │ • ML Analytics  │
                       └─────────────────┘
```

## 🔒 Security Features

- **JWT Authentication** with compact tokens (65% size reduction)
- **Role-Based Access Control (RBAC)** with fine-grained permissions
- **Comprehensive Audit Logging** for all user activities
- **Password Security** with BCrypt hashing
- **CORS Protection** with configurable origins
- **SQL Injection Prevention** with JPA/Hibernate
- **XSS Protection** with security headers
- **Session Management** with stateless JWT tokens

## 📊 Monitoring & Analytics

- **Real-time Security Dashboard** with metrics and charts
- **User Activity Monitoring** with behavioral analysis
- **Risk Assessment** with AI-powered scoring
- **Compliance Reporting** for SOX, HIPAA, GDPR
- **Anomaly Detection** for suspicious login patterns
- **Audit Trail** with comprehensive logging

---

## 🚀 Deployment

### Docker Deployment
```bash
# Build Docker image
docker build -t samap:latest .

# Run with Docker Compose
docker-compose up -d
```

### Cloud Deployment (AWS)
- **EC2** - Application hosting
- **RDS PostgreSQL** - Database
- **S3** - File storage
- **Lambda** - Serverless functions
- **SNS** - Notifications

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👨‍💻 Author

**Rishi Achavelli**
- GitHub: [@rishiachavelli](https://github.com/rishiachavelli)
- LinkedIn: [Rishi Achavelli](https://linkedin.com/in/rishiachavelli)
- Email: rishi.achavelli@example.com

---

## 🎯 Project Goals

This project demonstrates:
- **Enterprise Security Architecture** with modern best practices
- **Full-Stack Development** with Java/Spring Boot and modern frontend
- **AI Integration** for cybersecurity analytics
- **Cloud-Native Design** ready for AWS deployment
- **Professional Development** with proper documentation and testing

Perfect for showcasing skills in **cybersecurity**, **full-stack development**, **AI/ML integration**, and **enterprise software architecture**.

---

⭐ **Star this repository if you find it helpful!**
