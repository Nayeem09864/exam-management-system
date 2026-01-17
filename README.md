# Exam Management System

A simple CRUD application built with Spring Boot (Backend) and Angular (Frontend).

## Project Structure

```
exam-management-system/
├── backend/          # Spring Boot application
└── frontend/         # Angular application
```

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- Node.js 18+ and npm
- Angular CLI 17+

## Backend Setup

1. Navigate to the backend directory:
   ```bash
   cd backend
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

3. Run the Spring Boot application:
   ```bash
   mvn spring-boot:run
   ```

The backend will be available at `http://localhost:8080`

### Default Users
- **Admin**: username: `admin`, password: `admin123`
- **User**: username: `user`, password: `user123`

## Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   # or
   ng serve
   ```

The frontend will be available at `http://localhost:4200`

## Login Page

Currently implemented features:
- User authentication with username and password
- JWT token-based authentication
- Form validation
- Error handling
- Responsive login UI

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login endpoint
  - Request body: `{ "username": "string", "password": "string" }`
  - Response: `{ "token": "string", "username": "string", "role": "string", "message": "string" }`

## Development Notes

- The backend uses H2 in-memory database for development
- JWT tokens are stored in localStorage
- CORS is configured to allow requests from `http://localhost:4200`
