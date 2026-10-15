# RegLog Backend

Secure Spring Boot 3 REST API for registration, login, JWT HttpOnly cookie sessions, password reset OTP, and change password.

## Stack

- Java 17
- Spring Boot 3.3.5
- Spring Security, Spring Data JPA, Bean Validation, Spring Mail
- MySQL 8
- Flyway
- JWT (JJWT)

## Local setup

1. Create a MySQL database and user:

```sql
CREATE DATABASE reglog_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'reglog'@'localhost' IDENTIFIED BY 'your-password';
GRANT ALL PRIVILEGES ON reglog_db.* TO 'reglog'@'localhost';
FLUSH PRIVILEGES;
```

2. Copy `.env.example` to `.env` and fill the values, or export the same variables.

3. Start the backend:

```bash
mvn spring-boot:run
```

Default URL: `http://localhost:8080`

## Environment variables

| Variable | Description |
| --- | --- |
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD` | MySQL connection |
| `JWT_SECRET` | At least 32 characters |
| `JWT_ISSUER`, `JWT_ACCESS_TOKEN_MINUTES`, `JWT_COOKIE_NAME` | JWT settings |
| `COOKIE_SECURE`, `COOKIE_SAME_SITE`, `COOKIE_PATH` | Cookie settings |
| `FRONTEND_ORIGIN` | Exact frontend origin, e.g. `http://localhost:5173` |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | SMTP settings. Empty host starts the app but email sending fails safely. |
| `SERVER_PORT` | Server port |

## Database

Flyway runs automatically on startup and creates:

- `users`
- `auth_sessions`
- `password_resets`

Migration: `src/main/resources/db/migration/V1__init.sql`

Schema validation is enabled (`spring.jpa.hibernate.ddl-auto=validate`), so production schema is owned by Flyway.

## API endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/logout`
- `POST /api/auth/forgot-password`
- `POST /api/auth/verify-otp`
- `POST /api/auth/reset-password`
- `PUT /api/auth/change-password`
- `GET /api/users/me`
- `GET /api/csrf`

The JWT is never returned in JSON. It is stored in the HttpOnly `access_token` cookie.

## Security

- BCrypt password hashing.
- Short-lived JWT with a unique `jti`.
- Server-side `auth_sessions` records.
- Logout revokes the session and clears the cookie.
- Password reset stores only hashed OTP and reset token.
- OTP and reset token are never logged.
- CSRF uses Spring's cookie token repository. State-changing endpoints require the masked token in `X-XSRF-TOKEN`.
- CORS allows one exact frontend origin with credentials.
- CSRF public auth endpoints (register/login/forgot/verify/reset) are exempt.

## Email

SMTP is configured through environment variables. The application starts even when `MAIL_HOST` is empty; password-reset email requests fail with `503 Service Unavailable` instead of pretending success.

## Testing

```bash
mvn test
```

The project contains no large automated suite by design. Compilation, startup, database migration, registration, login, cookie access, logout replay, invalid credentials, duplicate data, OTP verification, reset, and change password were smoke-tested against MySQL.

## Running

```bash
mvn spring-boot:run
```
