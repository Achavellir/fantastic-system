# 🚀 SAMAP Deployment Guide

This guide provides comprehensive instructions for deploying SAMAP using GitHub Education benefits and various cloud platforms.

## 📋 Prerequisites

### GitHub Education Benefits
- GitHub Student Developer Pack
- GitHub Codespaces access
- GitHub Actions unlimited minutes
- GitHub Pages hosting
- GitHub Container Registry

### Required Accounts
- GitHub account with Education benefits
- Cloud provider account (AWS, DigitalOcean, or Heroku)
- Domain name (optional)

## 🎓 GitHub Education Setup

### 1. Enable GitHub Education Benefits

1. **Apply for GitHub Student Developer Pack:**
   - Visit: https://education.github.com/pack
   - Verify your student status
   - Get access to premium features

2. **Available Benefits for SAMAP:**
   - **GitHub Codespaces:** 180 hours/month free
   - **GitHub Actions:** Unlimited CI/CD minutes
   - **GitHub Pages:** Free static site hosting
   - **GitHub Container Registry:** Free Docker image hosting
   - **GitHub Advanced Security:** Security scanning

### 2. Repository Setup

```bash
# 1. Create new repository on GitHub
# 2. Clone your repository
git clone https://github.com/yourusername/samap.git
cd samap

# 3. Add SAMAP code
# Copy all the SAMAP files to your repository

# 4. Initial commit
git add .
git commit -m "Initial SAMAP implementation"
git push origin main
```

### 3. Configure Repository Settings

1. **Enable GitHub Pages:**
   - Go to Settings → Pages
   - Source: GitHub Actions
   - Custom domain (optional)

2. **Enable GitHub Actions:**
   - Go to Settings → Actions
   - Allow all actions and reusable workflows

3. **Configure Secrets:**
   ```
   Settings → Secrets and variables → Actions
   
   Add these secrets:
   - DB_PASSWORD: your_database_password
   - JWT_SECRET: your_jwt_secret_key
   - ADMIN_PASSWORD: your_admin_password
   ```

## 🌐 Deployment Options

### Option 1: GitHub Pages + Railway (Recommended)

**Frontend:** GitHub Pages (Free)
**Backend:** Railway (Free tier)
**Database:** Railway PostgreSQL (Free tier)

#### Step 1: Deploy Backend to Railway

1. **Create Railway Account:**
   - Visit: https://railway.app
   - Sign up with GitHub
   - Connect your repository

2. **Deploy Backend:**
   ```bash
   # Install Railway CLI
   npm install -g @railway/cli
   
   # Login to Railway
   railway login
   
   # Initialize project
   railway init
   
   # Add PostgreSQL database
   railway add postgresql
   
   # Deploy backend
   railway up
   ```

3. **Configure Environment Variables:**
   ```env
   SPRING_PROFILES_ACTIVE=production
   SPRING_DATASOURCE_URL=${{Postgres.DATABASE_URL}}
   JWT_SECRET=your_jwt_secret_here
   CORS_ALLOWED_ORIGINS=https://yourusername.github.io
   ```

#### Step 2: Deploy Frontend to GitHub Pages

1. **Update Frontend Environment:**
   ```typescript
   // frontend/src/environments/environment.prod.ts
   export const environment = {
     production: true,
     apiUrl: 'https://your-railway-app.railway.app/api'
   };
   ```

2. **GitHub Actions will automatically deploy to Pages**

### Option 2: Heroku (Free Tier)

#### Backend Deployment

1. **Create Heroku App:**
   ```bash
   # Install Heroku CLI
   # Create app
   heroku create samap-backend
   
   # Add PostgreSQL
   heroku addons:create heroku-postgresql:hobby-dev
   
   # Set environment variables
   heroku config:set SPRING_PROFILES_ACTIVE=production
   heroku config:set JWT_SECRET=your_jwt_secret
   ```

2. **Deploy:**
   ```bash
   git subtree push --prefix=backend heroku main
   ```

#### Frontend Deployment

1. **Build and Deploy:**
   ```bash
   cd frontend
   npm run build:prod
   
   # Deploy to GitHub Pages or Netlify
   ```

### Option 3: DigitalOcean App Platform

1. **Create DigitalOcean Account**
2. **Use App Platform:**
   - Connect GitHub repository
   - Configure build settings
   - Add database component
   - Deploy with one click

### Option 4: AWS Free Tier

#### Backend: AWS ECS/Fargate
#### Frontend: S3 + CloudFront
#### Database: RDS Free Tier

## 🔧 Configuration for Production

### 1. Environment Variables

Create production environment variables:

```env
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://host:port/database
SPRING_DATASOURCE_USERNAME=username
SPRING_DATASOURCE_PASSWORD=password

# JWT
JWT_SECRET=your-very-long-and-secure-jwt-secret-key
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=604800000

# Security
ADMIN_PASSWORD=secure_admin_password

# CORS
CORS_ALLOWED_ORIGINS=https://yourdomain.com,https://yourusername.github.io

# Logging
LOGGING_LEVEL_COM_SAMAP=INFO
```

### 2. Database Migration

```sql
-- Create production database
CREATE DATABASE samap_prod;
CREATE USER samap_user WITH PASSWORD 'secure_password';
GRANT ALL PRIVILEGES ON DATABASE samap_prod TO samap_user;
```

### 3. SSL/TLS Configuration

#### For Custom Domain:
```bash
# Get free SSL certificate from Let's Encrypt
certbot --nginx -d yourdomain.com
```

#### For GitHub Pages:
- Automatic HTTPS for github.io domains
- Custom domain HTTPS available

## 🚀 Automated Deployment with GitHub Actions

The included GitHub Actions workflow automatically:

1. **Tests** both backend and frontend
2. **Builds** Docker images
3. **Scans** for security vulnerabilities
4. **Deploys** to production
5. **Notifies** on completion

### Workflow Triggers:
- Push to `main` branch
- Pull requests to `main`
- Manual workflow dispatch

## 📊 Monitoring and Maintenance

### 1. Application Monitoring

```bash
# Check application health
curl https://your-backend-url/api/public/health

# Monitor logs
heroku logs --tail -a samap-backend  # For Heroku
railway logs                         # For Railway
```

### 2. Database Maintenance

```sql
-- Monitor database size
SELECT pg_size_pretty(pg_database_size('samap_prod'));

-- Clean old audit logs (if needed)
DELETE FROM audit_logs WHERE timestamp < NOW() - INTERVAL '1 year';
```

### 3. Security Updates

```bash
# Update dependencies regularly
mvn versions:display-dependency-updates
npm audit fix
```

## 🔒 Security Considerations

### 1. Environment Security
- Use strong passwords
- Rotate JWT secrets regularly
- Enable database SSL
- Use HTTPS everywhere

### 2. Access Control
- Limit database access
- Use least privilege principle
- Enable audit logging
- Monitor failed login attempts

### 3. Data Protection
- Regular database backups
- Encrypt sensitive data
- Implement data retention policies
- GDPR compliance measures

## 🎯 Professional Presentation

### 1. Custom Domain Setup

```bash
# Add CNAME record to your DNS
# For GitHub Pages:
yourdomain.com → yourusername.github.io

# For Railway:
api.yourdomain.com → your-railway-app.railway.app
```

### 2. Professional URLs

- **Frontend:** https://samap.yourdomain.com
- **Backend API:** https://api.samap.yourdomain.com
- **Documentation:** https://docs.samap.yourdomain.com

### 3. Demo Credentials

Create professional demo accounts:

```sql
-- Executive Demo Account
INSERT INTO users (username, email, first_name, last_name, password) 
VALUES ('executive', 'executive@samap-demo.com', 'Executive', 'User', '$2a$12$...');

-- Security Officer Demo Account
INSERT INTO users (username, email, first_name, last_name, password) 
VALUES ('security', 'security@samap-demo.com', 'Security', 'Officer', '$2a$12$...');
```

## 📈 Performance Optimization

### 1. Frontend Optimization
- Enable gzip compression
- Use CDN for static assets
- Implement lazy loading
- Optimize bundle size

### 2. Backend Optimization
- Database connection pooling
- Redis caching layer
- API response caching
- Database query optimization

### 3. Infrastructure Optimization
- Load balancing
- Auto-scaling
- Database read replicas
- Content delivery network

## 🎓 Interview Preparation

### 1. Demo Script

Prepare a 5-minute demo covering:
1. Login with different roles
2. Security dashboard overview
3. Real-time threat detection
4. Audit log analysis
5. User management features

### 2. Technical Discussion Points

- **Architecture decisions**
- **Security implementations**
- **Scalability considerations**
- **Technology choices**
- **Future enhancements**

### 3. Metrics to Highlight

- **Lines of code:** 10,000+
- **Test coverage:** 80%+
- **Security features:** 15+
- **API endpoints:** 25+
- **Database tables:** 6

## 🆘 Troubleshooting

### Common Issues:

1. **CORS Errors:**
   ```java
   // Update CORS configuration
   @CrossOrigin(origins = "https://yourdomain.com")
   ```

2. **Database Connection:**
   ```bash
   # Check database URL format
   jdbc:postgresql://host:port/database?sslmode=require
   ```

3. **JWT Token Issues:**
   ```bash
   # Verify JWT secret length (minimum 256 bits)
   echo "your-jwt-secret" | wc -c
   ```

## 📞 Support

- **Documentation:** GitHub Wiki
- **Issues:** GitHub Issues
- **Community:** GitHub Discussions

---

**🎉 Congratulations! Your SAMAP platform is now ready for professional deployment and demonstration!**
