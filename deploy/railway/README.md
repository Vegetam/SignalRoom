# Railway first: deployment runbook (Windows)

**Status:** Infrastructure deployment instructions only; not a production certification.
The application has outstanding functionality and security work documented in docs/DELIVERY_STATUS.md.

## Recommended media placement

Deploy **web and api** as two Railway services, with Railway managed PostgreSQL.
Use **LiveKit Cloud or a separately managed, publicly reachable LiveKit SFU** for media and TURN.
Do not assume a generic HTTP Railway web service is suitable for the SFU UDP port range.
LiveKit requires an externally reachable media path and correctly configured TURN/TLS.

## Prerequisites

1. Git repository with this `SignalRoom` directory.
2. Railway project and PostgreSQL service.
3. External LiveKit deployment, with API key, secret and secure `wss://` endpoint.
4. Persistent storage for `/data/uploads` in the API service; consider object storage before production.
5. Independent HTTPS domains for web and api.

## Railway UI configuration

Create a **web** service from the repository with Root Directory `/frontend` and Dockerfile `/frontend/Dockerfile`.
Create an **api** service with Root Directory `/backend` and Dockerfile `/backend/Dockerfile`.
Both Dockerfiles now honour the platform-assigned `PORT` environment variable.

API service variables (replace placeholders):

    DATABASE_URL=jdbc:postgresql://<host>:<port>/<db>
    SPRING_DATASOURCE_USERNAME=<user>
    SPRING_DATASOURCE_PASSWORD=<password>
    ALLOWED_ORIGIN=https://<web-domain>
    LIVEKIT_API_KEY=<secret-key-id>
    LIVEKIT_API_SECRET=<secret>
    LIVEKIT_PUBLIC_URL=wss://<livekit-domain>
    LIVEKIT_INTERNAL_URL=https://<livekit-domain>
    SIGNALROOM_FILES=/data/uploads

**Important:** Railway Postgres `DATABASE_URL` may be a `postgresql://...` connection string; the Java application expects a JDBC URL beginning with `jdbc:postgresql://`. Construct that JDBC URL from Railway's supplied host/port/database variables, and supply username/password separately. Do not publish credentials in repository files.

Attach a persistent Railway volume to `/data/uploads` on the API service. Set the API health check to `/actuator/health`.

Web service build variables (set **before** building; Next.js public variables are compiled into the client):

    NEXT_PUBLIC_API_URL=https://<api-domain>
    NEXT_PUBLIC_LIVEKIT_URL=wss://<livekit-domain>

Set web health check to `/` after establishing that the dashboard loads properly. If domains change, rebuild web.

## Windows CMD local test

    copy .env.example .env
    docker compose config
    docker compose up --build

## Smoke / acceptance gates

- `GET https://<api-domain>/actuator/health` reports healthy.
- Registration/login across two independent browser profiles.
- Team and channel creation, messaging persistence after logout.
- File upload/download after a restart and permissions denial for non-members.
- Host meeting scheduling, guest waiting-room approval, room-end control.
- Camera/screen-sharing call across different physical networks with TURN fallback.
- Concurrent-room cap and 60-member load tests in a suitable load-testing environment.
- Security, isolation, recovery and backup tests **must pass** before marking this production-ready.

## Railway config format

Railway has deprecated legacy `railway.json`/`railway.toml` in favour of `.railway/railway.ts` infrastructure as code. Follow current Railway documentation for managed project-level IaC. This project uses explicit service settings to avoid shipping an untested IaC schema.
