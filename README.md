# SplitSmart

A portfolio-ready, Splitwise-inspired expense sharing application. This first milestone implements a Spring Boot API for groups, members, equal expense splits, balances, and debt-simplification suggestions.

## Stack

- Java 21, Spring Boot 3, Spring Data JPA
- MySQL 8 (H2 profile for quick local development)
- React + Vite frontend scaffold
- Docker Compose for MySQL

## Run the backend

1. For MySQL: install Docker Desktop, start it, then run `docker compose up -d mysql`.
2. Until Docker is installed, the API automatically uses a file-backed H2 database in `data/`, so it can still run locally and data persists across restarts.
3. Run: `mvn spring-boot:run` after Maven is installed.

The API is available at `http://localhost:8080/api`. Swagger UI is at `/swagger-ui/index.html`.

## Core endpoints

- `POST /api/auth/register` and `POST /api/auth/login` — create an account or receive a JWT
- `POST /api/groups` — create a group
- `POST /api/groups/{groupId}/members` — invite/add a registered user by email
- `POST /api/groups/{groupId}/expenses` — record an equal split expense
- `GET /api/groups/{groupId}/balances` — view net balances
- `GET /api/groups/{groupId}/settlement-suggestions` — minimum debt transfers

## Authentication and invitations

All `/api/groups/**` routes require `Authorization: Bearer <accessToken>`. Member emails are normalized and unique. If an entered email belongs to an existing user, they join immediately. Otherwise a pending invitation is stored. Signing up with that same email automatically joins the invited groups.

Set `MAIL_ENABLED=true` and Spring Mail SMTP variables before sending real emails. In local development, invitation content is printed to the backend console instead of being sent.

## Intentional first-milestone boundary

Notifications, exact/percentage splits, settlement persistence, and payment-gateway webhooks are planned next.
