## Task 2 in file named with "customer app pseudocode.md"
# Turuq-Backend-Assessment

A modular, secure Spring Boot + MongoDB REST API for managing User Profiles with JWT
authentication, pagination/filtering, validation, and centralized error handling.

## Task breakdown & prioritization

1. **Data model & persistence** — `entities/UserEntity.java`, `repository/UserRepository.java`
2. **Business logic** — `service/UserService.java` (validation, dedupe, pagination)
3. **API layer** — `payloads/*`, `controller/UserController.java`
4. **Cross-cutting concerns** — `exception/*` (error handling), `security/*` + `config/SecurityConfig.java` (JWT auth)
5. **Non-functional hardening** — indexes, input sanitization, bounded pagination

Each layer only depends on layers above it in this list, which is why they were built in
this order.


## Running it

Prerequisites: JDK 17+, Maven, a running MongoDB instance.

```bash
# Point at your Mongo instance (defaults to mongodb://localhost:27017/turuqdb)
export MONGODB_URI="mongodb://localhost:27017/turuqdb"


mvn spring-boot:run
```

The service starts on `http://localhost:8080`.

## Authentication

All `/users/**` endpoints require a JWT. A default login account is **seeded
automatically the first time the app starts against a fresh database** (see
`config/DataSeeder.java`):

**login with **:-
- email: `user@example.com`
- password: `user123`


Get a token:
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@asd.csdscd","password":"user123"}'
```

Response:
```json
{ "token": "eyJhbGciOi...", "tokenType": "Bearer", "expiresInMs": 3600000 }
```

Then include the token on every `/users` call:
```bash
curl http://localhost:8080/users -H "Authorization: Bearer <token>"
```

## API reference

| Method | Path          | Description                                   | Auth |
|--------|---------------|------------------------------------------------|------|
| POST   | `/auth/login` | Exchange credentials for a JWT                  | No   |
| POST   | `/users`      | Create a user profile                           | Yes  |
| GET    | `/users`      | List profiles, paginated, optional `?age=`      | Yes  |
| GET    | `/users/{id}` | Fetch one profile                               | Yes  |
| PUT    | `/users/{id}` | Full update of a profile                        | Yes  |
| DELETE | `/users/{id}` | Delete a profile                                | Yes  |

### Create a user
```bash
curl -X POST http://localhost:8080/users \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"Ada Lovelace","email":"ada@example.com","age":36}'
```

### List users (paginated + filtered)
```bash
curl "http://localhost:8080/users?page=0&size=10&age=36" \
  -H "Authorization: Bearer <token>"
```

Response shape:
```json
{
  "items": [ { "id": "...", "name": "...", "email": "...", "age": 36, "createdAt": "..." } ],
  "page": 0,
  "size": 10,
  "totalElements": 1,
  "totalPages": 1
}
```

### Error shape (consistent across all failures)
```json
{
  "timestamp": "2026-07-23T10:15:30Z",
  "status": 409,
  "error": "Conflict",
  "message": "A user with email 'ada@example.com' already exists",
  "path": "/users"
}
```

## API documentation (Swagger / OpenAPI)

Interactive docs are served by springdoc and require no extra setup:

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Raw OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Both paths are public (no JWT needed to *view* the docs). To try protected endpoints
from within Swagger UI: call `POST /auth/login`, copy the `token` value, click
**Authorize** at the top of the page, and paste it in (no need to type `Bearer ` — the
scheme is pre-configured). Every endpoint has example request/response schemas and the
possible error status codes documented inline (see `@Operation`/`@ApiResponse`
annotations on `UserController` and `AuthController`).
`UserServiceTest` covers the duplicate-email rejection and not-found paths with Mongo
mocked out, so tests run fast with no external dependency.

