# Pulse Notification Service

An independent Kafka consumer used to demonstrate event-driven microservices.

It consumes `incident-events` in the `pulse-notifications` group and exposes the
latest 100 received notifications at `GET http://localhost:8081/api/notifications`.

Run Kafka before starting this service. From this directory:

```powershell
..\mvnw.cmd test
..\mvnw.cmd spring-boot:run
```

The service intentionally has no database in this milestone; its notification feed
is in-memory and resets when the process restarts. A later production version would
persist delivery attempts and connect an email, Slack, or paging provider.
