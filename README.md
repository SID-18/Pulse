# Pulse

## Continuous integration

GitHub Actions runs the backend and notification-service test suites, plus the
frontend lint and production build, for every push and pull request to `main`.

## API documentation

When Pulse is running, open `http://localhost:8080/swagger-ui.html` to browse
and try the API. Use `/api/auth/login` to obtain a JWT, then click **Authorize**
and paste the token to try protected endpoints. The machine-readable OpenAPI
description is available at `http://localhost:8080/v3/api-docs`.

## Run the complete platform with Docker

Docker Compose is an alternative to the Windows-local PostgreSQL, Memurai, Kafka,
and Vite setup. It starts isolated containers and does not modify the data in
your existing local PostgreSQL or Memurai instances.

Prerequisites:

- Docker Desktop is running.
- Ollama is running on Windows, with the configured model available (default:
  `qwen2.5:3b`).

Create your machine-local configuration once:

```powershell
Copy-Item -LiteralPath ".env.example" -Destination ".env"
```

Update `.env` with non-default local passwords and JWT secret, then start the
whole stack:

```powershell
docker compose up --build
```

Open `http://localhost:5173`. The frontend calls the backend through
`http://localhost:8080`; the backend calls Ollama through
`host.docker.internal:11434`.

The Compose database begins empty except for the Flyway schema. This is
intentional. The Docker-only profile creates one manager from the bootstrap
values in `.env`; it does not run in normal local or production profiles.
The default local login is `admin@pulse.local` with the password configured in
`PULSE_BOOTSTRAP_ADMIN_PASSWORD`. Change both values before sharing an
environment. Incident data should be provisioned explicitly.

To stop the containers without removing their data:

```powershell
docker compose down
```
