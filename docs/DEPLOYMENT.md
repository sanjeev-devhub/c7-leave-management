# Deployment Guide

This guide covers running the Camunda 7 embedded leave-management service beyond local development.

---

## 1. Local Docker (quickest)

```bash
docker compose -f docker/docker-compose.yml up --build
```

Two containers come up: `postgres` and `leave-management`. The app waits for Postgres to be healthy before starting. See the README for endpoints and credentials.

---

## 2. Standalone JAR + external Postgres

Build:

```bash
mvn clean package          # produces target/leave-management-1.0.0.jar
```

Run against any reachable Postgres:

```bash
export DB_URL=jdbc:postgresql://db.internal:5432/camunda
export DB_USERNAME=camunda_app
export DB_PASSWORD=*****
export CAMUNDA_ADMIN_USER=admin
export CAMUNDA_ADMIN_PASSWORD=*****

java -jar target/leave-management-1.0.0.jar
```

On first start the engine creates its own schema (`camunda.bpm.database.schema-update: true`) and Hibernate creates the `leave_audit_log` table. For stricter environments, see [Schema management](#5-schema-management).

---

## 3. Kubernetes (sketch)

The app is a stateless Spring Boot container; point it at a managed Postgres. A minimal Deployment:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: leave-management
spec:
  replicas: 1            # see note on multiple replicas below
  selector:
    matchLabels: { app: leave-management }
  template:
    metadata:
      labels: { app: leave-management }
    spec:
      containers:
        - name: app
          image: your-registry/leave-management:1.0.0
          ports:
            - containerPort: 8080
          env:
            - name: DB_URL
              value: jdbc:postgresql://postgres:5432/camunda
            - name: DB_USERNAME
              valueFrom: { secretKeyRef: { name: leave-db, key: username } }
            - name: DB_PASSWORD
              valueFrom: { secretKeyRef: { name: leave-db, key: password } }
            - name: CAMUNDA_ADMIN_PASSWORD
              valueFrom: { secretKeyRef: { name: leave-camunda, key: adminPassword } }
          readinessProbe:
            httpGet: { path: /actuator/health/readiness, port: 8080 }
            initialDelaySeconds: 30
          livenessProbe:
            httpGet: { path: /actuator/health/liveness, port: 8080 }
            initialDelaySeconds: 45
```

**Multiple replicas:** the Camunda 7 embedded engine supports clustering — several app instances can share one database and each runs a job executor. This works out of the box because job acquisition uses optimistic locking in the shared DB. Scaling horizontally is therefore safe; just ensure all replicas point at the same Postgres.

---

## 4. Observability

- **Health:** `/actuator/health` (with `liveness` / `readiness` groups).
- **Metrics:** `/actuator/metrics`, `/actuator/prometheus`.
- **Correlation:** every request carries an `X-Correlation-ID` (generated if absent) that appears on every log line — propagate it from your gateway for end-to-end tracing.
- **Engine insight:** Camunda Cockpit at `/camunda` shows live instances, incidents, and history.

Tighten `management.endpoints.web.exposure.include` for production so only the endpoints you want are reachable.

---

## 5. Schema management

For production, prefer controlled schema creation over `schema-update: true`:

1. Set `camunda.bpm.database.schema-update: false`.
2. Pre-create the Camunda schema using the official SQL DDL scripts shipped with the distribution (`create/postgres_engine_*.sql`), applied via Flyway/Liquibase or your DBA process.
3. Manage the `leave_audit_log` table with the same migration tool (set `spring.jpa.hibernate.ddl-auto: validate`).

A `db/migration` folder is included as the place to put Flyway scripts if you adopt that approach.

---

## 6. Production security checklist

> **This build ships with the REST API open (no authentication).** Before any non-local deployment you must add an auth layer in front of `/api/**` — e.g. Spring Security with JWT/OAuth2, or an API gateway that enforces authentication.

- [ ] **Add authentication** to the `/api/**` endpoints (gateway, Spring Security, or OAuth2). The API is currently open.
- [ ] **Change `admin`/`admin`** for the Camunda webapp; consider disabling the webapp entirely in production if you don't need Cockpit.
- [ ] **Terminate TLS** at your ingress/load balancer; never expose the API over plain HTTP.
- [ ] **Lock down `/actuator`** and `/camunda` to internal networks or authenticated operators.
- [ ] **Enable Camunda authorization** (`authorizationEnabled: true`) if you want per-user task visibility enforced by the engine itself, then assign authorizations to groups.
- [ ] **Rotate DB credentials** via your secrets manager; don't bake them into images.
- [ ] **Review history TTL** — the process sets `historyTimeToLive` to 180 days; adjust to your retention policy and enable history cleanup.

---

## 7. Backups

All state lives in Postgres (engine tables + `leave_audit_log`). Standard Postgres backup/restore (e.g. `pg_dump` / managed snapshots) is sufficient; there is no separate engine state to back up because the engine is embedded.
