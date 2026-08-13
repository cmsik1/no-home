# NoHome

NoHome is a full-stack housing search project with a Spring Boot backend, React/Vite frontend, PostgreSQL database, public data imports, Kakao Map integration, member authentication, notices, interest regions, and an AI assistant.

The refactored application follows a React `page -> hook -> service` flow and a Spring `controller -> application service -> persistence port -> JPA adapter` flow. Database changes are versioned with Flyway. See [the architecture notes](docs/refactoring-architecture.md) for the rationale and interview-ready explanation.

This repository is managed as a monorepo:

```text
NoHome/
  Backend/    Spring Boot API server
  Frontend/   React/Vite client
  docs/       current guides, work records, and archived project materials
```

Start with [the documentation index](docs/README.md) to distinguish current guides from historical records.

## Local Setup

Create a root `.env` file from the tracked example.

```powershell
Copy-Item .env.example .env
```

Fill in any keys you need for the features you want to test:

```text
PUBLIC_DATA_SERVICE_KEY
PUBLIC_DATA_APT_RENT_SERVICE_KEY
KAKAO_MAP_API_KEY
VITE_KAKAO_MAP_API_KEY
SSAFY_GMS_API_KEY
JWT_SECRET
```

The example file contains local-only placeholders. Do not commit real passwords, API keys, JWT secrets, or machine-specific settings.

Backend settings follow a fail-closed production contract. `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `JWT_COOKIE_SECURE=true` are required when the `prod` profile is active. Public-data, Kakao, and SSAFY GMS keys are optional at startup: leaving one empty disables or defers failure to that integration without blocking the base application build.

Tests inject an isolated test JWT and Testcontainers database connection. They do not require a personal `.env` file or real external API keys.

## Run With Docker Compose

Run the full stack from the repository root.

```powershell
docker compose up -d --build
```

To run only PostgreSQL for backend development:

```powershell
docker compose up -d postgres
```

Service URLs:

```text
Frontend: http://localhost:5173
Backend:  http://localhost:8080
Health:   http://localhost:8080/api/health
```

Useful commands:

```powershell
docker compose ps
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
docker compose down
```

To reset the local database volume:

```powershell
docker compose down -v
```

The former MySQL volume is not migrated or reused. Remove it separately only after confirming that its data is no longer needed.

## Development

Backend only:

```powershell
cd Backend
Copy-Item ..\.env .env
.\mvnw.cmd test
```

Frontend only:

```powershell
cd Frontend
npm install
npm test
npm run build
```

Flyway applies `Backend/src/main/resources/db/migration/V1__initial_schema.sql` when the backend starts. Add future schema changes as a new versioned migration; do not edit an already deployed migration. Backend integration tests use Testcontainers PostgreSQL and therefore require a running Docker engine.

## Git Layout

The root folder is the only Git repository. `Backend`, `Frontend`, and `docs` are regular subdirectories, so one root-level Git command can inspect or commit changes across documents, server code, and client code.

Original repository bundles are kept locally under `.monorepo-backup/` for recovery and are ignored by Git.
