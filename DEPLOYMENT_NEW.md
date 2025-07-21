# SAMAP Deployment Guide

This guide covers deployment options for the SAMAP platform.

## Prerequisites

- Java 17+
- Docker & Docker Compose
- PostgreSQL 14+
- Maven 3.8+

## Local Development

### Quick Start with Docker

1. Clone the repository:
```bash
git clone https://github.com/Achavellir/fantastic-system.git
cd fantastic-system
```

2. Start all services:
```bash
docker-compose up -d
```

3. Access the application:
- Application: http://localhost:8080
- Grafana: http://localhost:3000 (admin/admin123)
- RabbitMQ Management: http://localhost:15672 (admin/admin123)

### Manual Setup

1. Start PostgreSQL and create database:
```bash
sudo systemctl start postgresql
sudo -u postgres createdb samap_db
sudo -u postgres createuser samap_user
```

2. Configure database credentials in `application.yml`

3. Run the application:
```bash
mvn spring-boot:run
```

## Production Deployment

### Environment Variables

Set the following environment variables for production:

```bash
# Database
DB_PASSWORD=your_secure_password
POSTGRES_DB=samap_db
POSTGRES_USER=samap_user

# Redis (optional)
REDIS_PASSWORD=your_redis_password
REDIS_ENABLED=true

# JWT
JWT_SECRET=your_256_bit_secret_key
JWT_EXPIRATION=86400000

# Application
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

### Docker Production Deployment

1. Build the application:
```bash
mvn clean package -DskipTests
```

2. Build Docker image:
```bash
docker build -t samap:latest .
```

3. Run with production configuration:
```bash
docker run -d \
  --name samap \
  -p 8080:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_PASSWORD=your_password \
  -e JWT_SECRET=your_secret \
  samap:latest
```

### Cloud Deployment

#### AWS Deployment

1. Create RDS PostgreSQL instance
2. Create ElastiCache Redis cluster (optional)
3. Deploy application to ECS or EC2
4. Configure Application Load Balancer
5. Set up CloudWatch monitoring

#### DigitalOcean Deployment

1. Create PostgreSQL managed database
2. Create Redis managed database (optional)
3. Deploy to App Platform or Droplet
4. Configure load balancer
5. Set up monitoring

## Configuration

### Database Configuration

For production, update `application-prod.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://your-db-host:5432/samap_db
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
```

### Security Configuration

Ensure proper security settings:

```yaml
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000

spring:
  security:
    require-ssl: true
```

## Monitoring

### Health Checks

The application provides health check endpoints:

- `/actuator/health` - Application health
- `/actuator/health/db` - Database connectivity
- `/actuator/health/redis` - Redis connectivity

### Metrics

Prometheus metrics are available at:
- `/actuator/prometheus` - All metrics
- `/actuator/metrics` - Metric names

### Logging

Configure logging for production:

```yaml
logging:
  level:
    com.samap: INFO
    org.springframework.security: WARN
  file:
    name: logs/samap.log
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

## Backup and Recovery

### Database Backup

Create regular database backups:

```bash
# Create backup
pg_dump -h localhost -U samap_user samap_db > backup_$(date +%Y%m%d).sql

# Restore backup
psql -h localhost -U samap_user -d samap_db < backup_20240115.sql
```

### Application Data

Backup configuration files and logs:

```bash
tar -czf samap_config_$(date +%Y%m%d).tar.gz \
  application*.yml \
  docker-compose.yml \
  logs/
```

## Troubleshooting

### Common Issues

1. **Application won't start**
   - Check Java version (must be 17+)
   - Verify database connectivity
   - Check port availability

2. **Database connection failed**
   - Verify PostgreSQL is running
   - Check credentials and connection string
   - Ensure database exists

3. **Redis connection issues**
   - Verify Redis is running
   - Check Redis configuration
   - Disable Redis if not needed: `REDIS_ENABLED=false`

### Log Analysis

Check application logs for errors:

```bash
# View recent logs
tail -f logs/samap.log

# Search for errors
grep ERROR logs/samap.log

# Check startup logs
grep "Started AccessManagerApplication" logs/samap.log
```

## Security Considerations

### Production Security

1. Use strong passwords and secrets
2. Enable HTTPS/TLS
3. Configure firewall rules
4. Regular security updates
5. Monitor access logs
6. Implement backup strategy

### Environment Security

- Store secrets in environment variables
- Use secure secret management systems
- Rotate credentials regularly
- Monitor for security vulnerabilities
- Keep dependencies updated

This deployment guide provides the essential information needed to deploy SAMAP in various environments while maintaining security and performance best practices.
