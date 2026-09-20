# Pulse

## Continuous integration

GitHub Actions runs the backend and notification-service test suites, plus the
frontend lint and production build, for every push and pull request to `main`.

## API documentation

When Pulse is running, open `http://localhost:8080/swagger-ui.html` to browse
and try the API. Use `/api/auth/login` to obtain a JWT, then click **Authorize**
and paste the token to try protected endpoints. The machine-readable OpenAPI
description is available at `http://localhost:8080/v3/api-docs`.

## Observability

Pulse exposes lightweight operational endpoints through Spring Boot Actuator:

- `GET /actuator/health` — public liveness/readiness health summary, suitable
  for Docker or a load balancer.
- `GET /actuator/info` — public non-sensitive application metadata.
- `GET /actuator/prometheus` — Prometheus-format metrics. This requires an
  administrator or manager JWT because metrics can reveal operational details.

The health endpoint checks the application's dependencies, including the
database, Redis, and Kafka. Detailed health information is returned only to an
authenticated administrator or manager.

## Metrics dashboard

Docker Compose can also start a local Prometheus and Grafana stack. Prometheus
scrapes the backend every 15 seconds using a dedicated `pulse-metrics` account;
Grafana is provisioned with the **Pulse Overview** dashboard.

Before the first dashboard startup, create the local Prometheus password file.
Its value must match `PULSE_METRICS_PASSWORD` in `.env`:

```powershell
Copy-Item -LiteralPath ".\\observability\\prometheus\\metrics-password.example" -Destination ".\\observability\\prometheus\\metrics-password"
```

The example uses a development-only password. Change it in both `.env` and the
untracked `metrics-password` file before sharing an environment. Then run:

```powershell
docker compose up --build
```

Open Prometheus at `http://localhost:9090/targets` and confirm both Pulse
services are **UP**. Open Grafana at `http://localhost:3000`, sign in with the
`GRAFANA_ADMIN_USER` and `GRAFANA_ADMIN_PASSWORD` values from `.env`, then open
**Dashboards → Pulse → Pulse Overview**.

## Alerting

Prometheus evaluates Pulse alert rules every 15 seconds and sends active alerts
to Alertmanager. The initial local rules detect an unavailable backend or
notification service, sustained HTTP 5xx responses, and database connection
pool pressure. Alertmanager is intentionally configured as a local dashboard
only; it does not claim to send email, Slack, or PagerDuty notifications.

Open `http://localhost:9090/alerts` to see alert-rule state and
`http://localhost:9093` to see alerts routed to Alertmanager. A production
deployment would add a real receiver and store its credentials outside Git.

## Distributed tracing

Pulse uses Spring Boot's OpenTelemetry support to create traces for HTTP,
database, and observed Kafka work. Docker Compose sends those traces to an
OpenTelemetry Collector, which batches and stores them in Tempo. Grafana has a
provisioned **Pulse Tempo** data source; use **Explore**, select that data
source, and search for `service.name = "pulse"` or
`service.name = "pulse-notification-service"`.

Sampling is set to 100% for this local learning environment. Production systems
normally choose a lower rate or tail-based sampling to control storage costs.

## Run the complete platform with Docker

Docker Compose is an alternative to the Windows-local PostgreSQL, Memurai, Kafka,
and Vite setup. It starts isolated containers and does not modify the data in
your existing local PostgreSQL or Memurai instances.

Prerequisites:

- Docker Desktop is running.
- Ollama is running on Windows, with the configured model available (default:
  `qwen2.5:1.5b`).
- Ollama has the local embedding model used by RAG:

```powershell
ollama pull embeddinggemma
```

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
`host.docker.internal:11434`. Docker Compose also starts a local Python AI
service and ChromaDB. ChromaDB persists vector data in the `chroma-data`
Docker volume; it is never sent to a paid cloud service.

## RAG recommendations

Pulse keeps PostgreSQL as the source of truth. ChromaDB stores only searchable
vector representations of resolved incidents. A manager first indexes that
history with:

```http
POST /api/incidents/ai/reindex-resolved
```

Then any signed-in user can request a grounded recommendation for an incident:

```http
GET /api/incidents/{incidentId}/ai-recommendation
```

The Python AI service embeds the current incident through local Ollama, finds
up to three similar resolved incidents in ChromaDB, and sends only that context
to local Ollama for a recommendation. If no resolved incidents have been
indexed, the endpoint still returns a recommendation but clearly reports that
there is no retrieved history.

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
