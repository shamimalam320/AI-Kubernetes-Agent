# AI Kubernetes Agent - Build & Run Guide

## 🚀 Quick Start

This guide will help you build and run the AI Kubernetes Agent with the new authentication system.

---

## 📋 Prerequisites

### Required Software
- **Docker** & **Docker Compose** (for PostgreSQL database)
- **Java 21** (for Spring Boot backend)
- **Node.js 18+** & **npm** (for React frontend)
- **kubectl** (configured with a Kubernetes cluster)

### Environment Setup
1. Ensure Docker is running
2. Verify Java version: `java -version` (should be 21+)
3. Verify Node version: `node -version` (should be 18+)
4. Verify kubectl access: `kubectl cluster-info`

---

## 🔧 Step 1: Database Setup

### Start PostgreSQL Container

```bash
# Navigate to project root
cd AI-Kubernetes-Agent

# Start PostgreSQL using Docker Compose
docker compose up -d postgres

# Verify PostgreSQL is running
docker compose ps
```

The database will be available at:
- **Host**: `localhost`
- **Port**: `5432`
- **Database**: `k8s_agent`
- **Username**: `k8s_user`
- **Password**: `k8s_password`

### Verify Database Connection

```bash
# Connect to PostgreSQL (optional)
docker exec -it ai-kubernetes-agent-postgres-1 psql -U k8s_user -d k8s_agent

# Inside psql, check tables (after first backend run)
\dt

# Exit psql
\q
```

---

## 🔧 Step 2: Backend Setup

### Configure Environment Variables

```bash
cd backend

# Create .env file from example
cp .env.example .env

# Edit .env and set your values:
# - DATABASE_URL (already set for local PostgreSQL)
# - JWT_SECRET (generate a secure random string)
# - JWT_EXPIRATION (default: 86400000 = 24 hours)
# - OPENROUTER_API_KEY (your OpenRouter API key)
```

**Example `.env` file:**
```properties
# Database Configuration
DATABASE_URL=jdbc:postgresql://localhost:5432/k8s_agent
DATABASE_USERNAME=k8s_user
DATABASE_PASSWORD=k8s_password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production
JWT_EXPIRATION=86400000

# OpenRouter AI Configuration
OPENROUTER_API_KEY=your-openrouter-api-key-here
```

### Build and Run Backend

```bash
# Make init script executable (first time only)
chmod +x init-gradle.sh

# Initialize Gradle wrapper (first time only)
./init-gradle.sh

# Build the application
./gradlew clean build

# Run the application
./gradlew bootRun
```

The backend will start on **http://localhost:8080**

### Verify Backend is Running

```bash
# Health check
curl http://localhost:8080/actuator/health

# Expected response:
# {"status":"UP"}
```

---

## 🔧 Step 3: Frontend Setup

### Install Dependencies

```bash
# Navigate to frontend directory
cd ../frontend

# Install npm packages
npm install
```

### Configure Environment Variables

```bash
# Create .env.local file
cat > .env.local << EOF
VITE_API_BASE_URL=http://localhost:8080
EOF
```

### Run Frontend Development Server

```bash
# Start Vite dev server
npm run dev
```

The frontend will start on **http://localhost:5173**

---

## 🎯 Step 4: Test the Application

### 1. Register a New User

1. Open browser: **http://localhost:5173**
2. You'll be redirected to the login page
3. Click **"Sign up"** to go to registration
4. Fill in the form:
   - Username: `testuser`
   - Email: `test@example.com`
   - Password: `password123`
   - Confirm Password: `password123`
5. Click **"Create Account"**
6. You'll be automatically logged in and redirected to the dashboard

### 2. Verify Authentication

After successful registration/login, you should see:
- Dashboard with your username and email in the top-right
- System status indicator
- "Investigate Cluster" button
- Logout button

### 3. Test Kubernetes Investigation

1. Ensure your kubectl is configured with a cluster
2. Click **"Investigate Cluster"** button
3. The system will:
   - Check pod status
   - Collect logs
   - Analyze events
   - Use AI to diagnose issues
4. View the investigation results

### 4. Test Logout

1. Click the **"Logout"** button in the top-right
2. You'll be redirected to the login page
3. Your JWT token will be cleared

### 5. Test Login

1. On the login page, enter your credentials:
   - Username: `testuser`
   - Password: `password123`
2. Click **"Sign In"**
3. You'll be redirected back to the dashboard

---

## 🐳 Step 5: Run with Docker Compose (Full Stack)

### Build and Run Everything

```bash
# Navigate to project root
cd AI-Kubernetes-Agent

# Build and start all services
docker compose up --build

# Or run in detached mode
docker compose up --build -d
```

This will start:
- **PostgreSQL** on port 5432
- **Backend** on port 8080
- **Frontend** on port 5173

### Stop All Services

```bash
# Stop all containers
docker compose down

# Stop and remove volumes (clears database)
docker compose down -v
```

---

## 🔍 Troubleshooting

### Backend Issues

**Problem**: Backend fails to start with database connection error

**Solution**:
```bash
# Check if PostgreSQL is running
docker compose ps

# Check PostgreSQL logs
docker compose logs postgres

# Restart PostgreSQL
docker compose restart postgres
```

**Problem**: JWT authentication fails

**Solution**:
- Verify `JWT_SECRET` is set in `backend/.env`
- Check that the secret is at least 32 characters long
- Restart the backend after changing `.env`

### Frontend Issues

**Problem**: Frontend can't connect to backend

**Solution**:
- Verify backend is running: `curl http://localhost:8080/actuator/health`
- Check `VITE_API_BASE_URL` in `frontend/.env.local`
- Clear browser cache and reload

**Problem**: TypeScript errors in IDE

**Solution**:
```bash
cd frontend
npm install
# Restart your IDE/VSCode
```

### Database Issues

**Problem**: Database tables not created

**Solution**:
- The backend automatically creates tables on first run (Hibernate DDL auto)
- Check backend logs for any errors
- Verify database connection in `application.yml`

**Problem**: Need to reset database

**Solution**:
```bash
# Stop and remove database volume
docker compose down -v

# Start fresh
docker compose up -d postgres
```

---

## 📊 API Endpoints

### Authentication Endpoints (Public)

```bash
# Register new user
POST http://localhost:8080/api/v1/auth/register
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}

# Login
POST http://localhost:8080/api/v1/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password123"
}

# Validate token
GET http://localhost:8080/api/v1/auth/validate
Authorization: Bearer <your-jwt-token>
```

### Investigation Endpoints (Protected)

```bash
# Trigger investigation
POST http://localhost:8080/api/v1/investigate
Authorization: Bearer <your-jwt-token>

# Get investigation history
GET http://localhost:8080/api/v1/investigate/history
Authorization: Bearer <your-jwt-token>

# Get specific investigation
GET http://localhost:8080/api/v1/investigate/history/{id}
Authorization: Bearer <your-jwt-token>
```

### Health Check (Public)

```bash
# Application health
GET http://localhost:8080/actuator/health

# Investigation service health
GET http://localhost:8080/api/v1/investigate/health
```

---

## 🔐 Security Notes

### JWT Token
- Tokens expire after 24 hours (configurable via `JWT_EXPIRATION`)
- Stored in browser localStorage
- Automatically included in API requests
- Cleared on logout

### Password Security
- Passwords are hashed using BCrypt
- Minimum length: 6 characters (enforced in frontend)
- Never stored in plain text

### CORS Configuration
- Frontend origin (`http://localhost:5173`) is whitelisted
- Credentials are allowed for cookie-based auth (if needed)
- Configure additional origins in `CorsConfig.java`

---

## 📝 Development Tips

### Hot Reload

**Backend**: Spring Boot DevTools is enabled
- Changes to Java files trigger automatic restart
- Static resources reload without restart

**Frontend**: Vite HMR (Hot Module Replacement)
- Changes to React components update instantly
- No page refresh needed

### Database Inspection

```bash
# View database logs
docker compose logs -f postgres

# Connect to database
docker exec -it ai-kubernetes-agent-postgres-1 psql -U k8s_user -d k8s_agent

# Useful SQL commands
\dt                    # List tables
\d users              # Describe users table
SELECT * FROM users;  # View all users
```

### Backend Logs

```bash
# View backend logs (if running with Docker Compose)
docker compose logs -f backend

# View logs (if running with Gradle)
# Logs appear in terminal where you ran ./gradlew bootRun
```

---

## 🚀 Production Deployment

### Environment Variables for Production

```properties
# Use strong, randomly generated secrets
JWT_SECRET=<generate-with-openssl-rand-base64-32>

# Use production database URL
DATABASE_URL=jdbc:postgresql://prod-db-host:5432/k8s_agent

# Set appropriate expiration
JWT_EXPIRATION=3600000  # 1 hour for production

# Use production OpenRouter key
OPENROUTER_API_KEY=<your-production-key>
```

### Security Checklist

- [ ] Change default database credentials
- [ ] Use strong JWT secret (32+ characters)
- [ ] Enable HTTPS/TLS
- [ ] Configure proper CORS origins
- [ ] Set up database backups
- [ ] Enable rate limiting
- [ ] Configure proper logging
- [ ] Set up monitoring and alerts

---

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Documentation](https://spring.io/projects/spring-security)
- [React Documentation](https://react.dev/)
- [Vite Documentation](https://vitejs.dev/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [Kubernetes Documentation](https://kubernetes.io/docs/)

---

## 🆘 Getting Help

If you encounter issues:

1. Check the troubleshooting section above
2. Review application logs
3. Verify all prerequisites are installed
4. Ensure environment variables are set correctly
5. Check that all services are running

---

**Built with ❤️ using Spring Boot, React, and PostgreSQL**