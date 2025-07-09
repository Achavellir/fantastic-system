# Secure Access Management & Audit Platform (SAMAP)

## 🔐 Overview
SAMAP is a full-stack, cloud-ready platform that combines **user access control**, **role-based permissions**, and **real-time security auditing**. Designed with cybersecurity principles, the system monitors access behavior, logs all activity, and uses **AI-based anomaly detection** to flag potential threats.

Built using:
- **Java + Spring Boot** (secure REST APIs)
- **Angular** (dashboard and access manager)
- **AWS S3 + Lambda + SNS** (logging and alerts)
- **PostgreSQL** (user and role storage)
- **AI**: Python microservice for real-time anomaly detection in login behavior (failed attempts, location mismatch, login time anomalies)

---

## 🎯 Goals
- ✅ Role-based access control (RBAC) with JWT authentication
- ✅ Full audit logging of all user actions
- ✅ Admin dashboard with filtering, sorting, exportable logs
- ✅ AI-powered risk scoring engine for access events
- ✅ Alerts on high-risk activity via AWS SNS or email

---

## 📐 Architecture

      [ Angular Admin UI ]
             |
      [Spring Boot REST APIs]
             |
 ┌────────────┬──────────────┐
 |         PostgreSQL        |
 |  User, Role, Audit Logs   |
 └────────────┴──────────────┘
             |
   [Python AI Microservice]
             |
  [AWS SNS] ← Anomaly Alerts
             |
         [Admin Email]



---

## 🚀 Tech Stack
- Backend: Java, Spring Boot, Spring Security, JWT
- Frontend: Angular, TypeScript, HTML5, Chart.js
- AI: Python (Scikit-learn or PyOD) served via REST API
- Cloud: AWS S3, SNS, Lambda (optional)
- DevOps: Docker, Jenkins, IntelliJ, GitHub Actions

