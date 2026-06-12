# Annual Leave Request Management — Camunda 7 (Embedded)

A workflow-driven leave-request system built on the **embedded Camunda 7 engine** inside a Spring Boot application, backed by **PostgreSQL**.

An employee submits a leave request, a line manager reviews it, and the manager can **approve**, **reject**, or **request more information**. Approved requests are logged to an audit table by a service-task delegate. A "more info" decision loops the process back to the employee and then to review again.

> **This build has no authentication.** The REST API is open so you can run the workflow end-to-end immediately. JWT/Spring Security and the automated test suite were intentionally removed to keep this a minimal, working baseline. The Camunda webapp under `/camunda` still uses its own login (admin/admin).

---

## Stack

| Concern            | Choice                                              |
| ------------------ | --------------------------------------------------- |
| Workflow engine    | Camunda 7.24.0, **embedded** (runs in the JVM)      |
| Framework          | Spring Boot 3.5.5                                    |
| Language / runtime | Java 17                                              |
| Database           | PostgreSQL 16 (Camunda schema + app audit tables)   |
| Engine UIs         | Camunda Cockpit / Tasklist / Admin at `/camunda`    |
| Docs               | OpenAPI / Swagger UI                                 |
| Build              | Maven                                               |

> **Note on Camunda 7 support:** 7.24.0 is the last community-edition release published to Maven Central. It is stable and fully functional; there simply won't be newer community 7.x releases.

---

## Architecture

The embedded engine means there is **no broker to run** — the process engine is a set of beans inside this Spring Boot app, persisting to the same Postgres database as the app's own tables.

### The process

```
Employee lane:   (start) → Request Annual Leave ──┐         ┌── Provide Additional Information ──┐
                                                  ▼         │ ▲                                  │
Line-Manager lane:                          Review Request ─┴─┤ Decision ├── APPROVED → Log Request → (Approved)
                                                               │          ├── REJECTED ──────────────→ (Rejected)
                                                               │          └── MORE_INFO_REQUIRED ──────┘ (loops up)
```

Decisions are driven by the `decision` process variable: `APPROVED` / `REJECTED` / `MORE_INFO_REQUIRED`.

### Engine integration points

| Operation       | Camunda 7 API                                  |
| --------------- | ---------------------------------------------- |
| Start instance  | `RuntimeService.startProcessInstanceByKey()`   |
| User tasks      | `TaskService` (active) + `HistoryService` (done) |
| Variables       | `RuntimeService.getVariables/setVariables`     |
| Service task    | `JavaDelegate` (`LogRequestDelegate`)          |

---

## Running with Docker

The only infrastructure dependency is Postgres — everything else is in the app.

```bash
docker compose -f docker/docker-compose.yml up --build
```

This starts two containers:

| Component        | URL / Address                          | Credentials        |
| ---------------- | -------------------------------------- | ------------------ |
| App / REST API   | http://localhost:8080                  | none (open)        |
| Swagger UI       | http://localhost:8080/swagger-ui.html  | none (open)        |
| Camunda Webapp   | http://localhost:8080/camunda          | `admin` / `admin`  |
| PostgreSQL       | localhost:5432                         | `camunda`/`camunda`|

Tear down (and wipe the database volume):

```bash
docker compose -f docker/docker-compose.yml down -v
```

---

## Running locally without Docker

Start a Postgres instance (or point at an existing one), then:

```bash
export DB_URL=jdbc:postgresql://localhost:5432/camunda
export DB_USERNAME=camunda
export DB_PASSWORD=camunda
mvn spring-boot:run
```

---

## Demo users (Camunda Tasklist / candidate-group routing)

These are seeded into Camunda's identity service at startup so the Camunda Tasklist works and candidate-group task routing resolves. They are **not** required to call the REST API (which is open).

| Username    | Password        | Camunda group  |
| ----------- | --------------- | -------------- |
| `emp01`     | `demo-password` | employee       |
| `emp02`     | `demo-password` | employee       |
| `manager01` | `demo-password` | line-manager   |
| `manager02` | `demo-password` | line-manager   |
| `hr01`      | `demo-password` | —              |
| `admin`     | `demo-password` | —              |

---

## API reference

All paths are prefixed `/api`. Bodies are JSON. Full schemas live in Swagger UI.

| Method & path                      | Purpose                                  |
| ---------------------------------- | ---------------------------------------- |
| `POST /leave/start`                | Start a leave process instance           |
| `POST /tasks/search`               | Search user tasks (active or completed)  |
| `GET  /tasks/{id}`                 | Get a task by id                         |
| `POST /tasks/{id}/assign`          | Assign a task                            |
| `POST /tasks/{id}/unassign`        | Unassign a task                          |
| `POST /tasks/{id}/complete`        | Complete a task (with variables)         |
| `POST /variables/search`           | Read variables for an instance           |
| `POST /variables`                  | Set/update variables on an instance      |
| `GET  /audit`                      | List audit log entries                   |
| `GET  /audit/{id}`                 | Get a single audit entry                 |

### A complete walkthrough

```bash
BASE=http://localhost:8080

# 1. Start a request
PID=$(curl -s -X POST $BASE/api/leave/start \
  -H "Content-Type: application/json" \
  -d '{"employeeId":"EMP-001","employeeName":"John Doe","leaveType":"ANNUAL_LEAVE",
       "startDate":"2026-06-01","endDate":"2026-06-05","reason":"Holiday","daysRequested":5}' \
  | jq -r .processInstanceId)

# 2. Complete the "Request Annual Leave" task
REQ_TASK=$(curl -s -X POST $BASE/api/tasks/search \
  -H "Content-Type: application/json" \
  -d "{\"processInstanceId\":\"$PID\",\"candidateGroup\":\"employee\"}" | jq -r '.items[0].taskId')
curl -s -X POST $BASE/api/tasks/$REQ_TASK/complete \
  -H "Content-Type: application/json" -d '{}'

# 3. Find the review task and approve it
REVIEW_TASK=$(curl -s -X POST $BASE/api/tasks/search \
  -H "Content-Type: application/json" \
  -d "{\"processInstanceId\":\"$PID\",\"candidateGroup\":\"line-manager\"}" | jq -r '.items[0].taskId')
curl -s -X POST $BASE/api/tasks/$REVIEW_TASK/complete \
  -H "Content-Type: application/json" \
  -d '{"variables":{"decision":"APPROVED","managerComments":"Enjoy!"}}'

# 4. The Log Request delegate has now written an audit row
curl -s $BASE/api/audit | jq
```

---

## Configuration

Set via environment variables (defaults in parentheses):

| Variable                  | Default                                  | Description                          |
| ------------------------- | ---------------------------------------- | ------------------------------------ |
| `DB_URL`                  | `jdbc:postgresql://localhost:5432/camunda` | JDBC URL                           |
| `DB_USERNAME`             | `camunda`                                | DB user                              |
| `DB_PASSWORD`             | `camunda`                                | DB password                          |
| `CAMUNDA_ADMIN_USER`      | `admin`                                  | Camunda webapp admin id              |
| `CAMUNDA_ADMIN_PASSWORD`  | `admin`                                  | Camunda webapp admin password        |

---

## Project layout

```
src/main/java/com/example/leavemanagement/
├── LeaveManagementApplication.java   # @SpringBootApplication @EnableProcessApplication
├── controller/                       # REST endpoints (open, no auth)
├── service/                          # RuntimeService/TaskService/HistoryService wrappers
├── delegate/                         # LogRequestDelegate (service task)
├── mapper/                           # Task → TaskResponse mapping
├── dto/                              # request/response payloads
├── model/                            # Decision enum + AuditLog JPA entity
├── exception/                        # custom exceptions + global handler
├── listener/                         # CamundaIdentitySeeder (seeds engine users/groups)
├── config/                           # OpenAPI config
└── util/                             # correlation-id filter
src/main/resources/
├── processes/annual-leave-request.bpmn
├── application.yml
└── logback-spring.xml
```

See `docs/DEPLOYMENT.md` for production deployment guidance.

---

## Troubleshooting

**`Group has an invalid id: 'line-manager' is not a valid resource identifier`**

Since Camunda 7.10, user/group/tenant ids are validated against a whitelist pattern that defaults to `[a-zA-Z0-9]+|camunda-admin` — which rejects the hyphen in `line-manager`. This project widens the pattern in `application.yml`:

```yaml
camunda:
  bpm:
    generic-properties:
      properties:
        generalResourceWhitelistPattern: "[a-zA-Z0-9_.-]+"
```

If you hit this on a database that already partially initialised from an earlier failed run, do a clean restart so the seeder runs against a fresh schema:

```bash
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up --build
```
