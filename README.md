# SignalRoom

**Self-hostable Teams-style collaboration and video conferencing — Next.js, Java Spring Boot and WebRTC**

> **Development status: active development / experimental.** SignalRoom is an independently built collaboration application inspired by the workflow of Microsoft Teams and other conferencing products. It is **not affiliated with Microsoft, Zoom or Google**, does **not** currently have feature parity with those products, and **must not be treated as production-certified**.

SignalRoom combines team workspaces, channels and messaging with browser-based video meetings. It uses **LiveKit as its internal WebRTC selective forwarding unit (SFU)**, while a **Java 21 / Spring Boot API** manages collaboration data and meeting admission, and a **Next.js / React** application supplies the interface.

## Features and current status

| Area | Current source implementation / limitation |
| --- | --- |
| Accounts | Local registration and login; no enterprise SSO yet |
| Teams and channels | Create teams/channels, invite registered members, access workspace content |
| Channel chat | Persisted channel messages; private/group DMs, threaded replies and reactions are not yet complete |
| Files | Upload/download within workspaces; local persistent volume in Docker, not production object storage |
| Calendar | Create and view events; full invitation, reminders and recurring meetings are not yet complete |
| Meetings | Room creation/join, audio/video, screen sharing and participant UI powered by LiveKit |
| Host moderation | Meeting-specific host controls and waiting-room admission logic; workspace identity and meeting identity are not fully unified |
| Meeting history | Backend meeting records; verify frontend coverage before relying on it |
| Capacity | Configured **60-person meeting admission limit** (including host); 60 concurrent users **have not been load tested** |
| Diagnostics | Developer-oriented browser WebRTC statistics and protocol tracing; not a full packet-level analyzer |
| Production security | **Not ready:** see Security section |

The goal is a polished, reliable Teams-style alternative, but functionality should be evaluated against the code and tests rather than assumed complete.

## Architecture

```text
                  Browser / Next.js (React, TypeScript)
                    | HTTPS / REST         | WSS / WebRTC
                    v                      v
           Java 21 / Spring Boot API     LiveKit SFU
             |           |                |        |
             v           v                v        v
         PostgreSQL   Uploaded files     Redis   WebRTC media
                       (Docker volume)            (ICE, DTLS-SRTP,
                                                   RTP/RTCP, BWE)

          coturn: standalone development TURN service
          (not yet integrated into LiveKit media routing)
```

**Responsibilities:**

- **Frontend (`frontend/`)** — Next.js workspace, account UI, channel messaging, calendar, meeting lobby and room UI.
- **Backend (`backend/`)** — Spring Boot endpoints for user accounts, membership, messages, files, calendar events and meeting admission/tokens.
- **PostgreSQL** — Collaboration and meeting metadata.
- **LiveKit SFU** — ICE, DTLS-SRTP, RTP/RTCP, media forwarding and adaptive media transport.
- **Redis** — LiveKit coordination in the development stack.
- **coturn** — A separately configured TURN container. Its existence **does not mean LiveKit TURN fallback is already correctly wired**.

## Prerequisites

- **Windows 10/11 with Docker Desktop**, running Linux containers and Docker Compose v2.
- Enough resources for multiple Docker services; allow browser camera/microphone permissions.
- Ports **3000, 8080, 7880, 7881, 3478**, plus configured UDP media ports, must not be occupied by other processes.

macOS/Linux can also run Compose, but the commands below are **Windows CMD commands**.

## Quick start — Windows CMD

1. Download or clone this repository and open **Command Prompt** in the directory containing `docker-compose.yml`.
2. Create the development environment file (first run only):

   ```cmd
   copy .env.example .env
   ```

3. Check Compose configuration, build and launch:

   ```cmd
   docker compose config
   docker compose up --build
   ```

4. Visit **http://localhost:3000**. Register a local test account, then use the dashboard to create a team and channel. Navigate to **Meet** for conferencing.
5. Test the meeting in a separate browser profile or with a second device. Grant camera/microphone permissions when prompted.

> `.env.example` includes **insecure local development credentials**, not production secrets. Change them for any non-local deployment. Do not commit your actual `.env` file.

### Local URLs

| Purpose | URL |
| --- | --- |
| App | http://localhost:3000 |
| Workspace dashboard | http://localhost:3000/dashboard |
| Meeting lobby | http://localhost:3000/meet |
| Meeting room | `http://localhost:3000/room/<room-id>` |
| Spring Boot API | http://localhost:8080 |
| API health (when Actuator is enabled) | http://localhost:8080/actuator/health |
| LiveKit signalling (WebSocket, not a web page) | ws://localhost:7880 |

### Windows troubleshooting

```cmd
docker compose ps
docker compose logs --tail=150 api
docker compose logs --tail=150 web
docker compose logs --tail=150 sfu
docker compose up --build
```

To stop services: press **Ctrl+C**, or execute `docker compose down` in another CMD window.

**Data warning:** `docker compose down -v` deletes named volumes, including the local database and uploaded files. Do **not** use it unless you intend to reset all local data.

If the meeting area appears blank, examine browser DevTools **Console** and **Network**, then review `api`, `web` and `sfu` logs. A successful frontend build does not automatically prove camera, media transport or multi-user operation.

## Tests and validation

The project includes Java tests and frontend source-level tests. To run them **where the appropriate tools are installed**:

```cmd
cd frontend
npm install
npm test
npm run build
cd ..\backend
mvn test
cd ..
```

Or build the integrated Docker application:

```cmd
docker compose build --no-cache
docker compose up
```

**Known validation status:** A recent Windows Docker run demonstrated a successful Next.js production build; the developer subsequently reported local operation after backend fixes. These observations are **not** independent end-to-end verification, an audited security review or proof of 60-user media capacity. Test suites and production builds must be rerun for each release.

Before public production launch, validate at minimum: user authorization, teams/files isolation, meeting admissions, browser interoperability, network interruption/reconnection, recording if added, backups, and **60 concurrent participants under representative video/network conditions**.

## Deployment roadmap

The planned rollout order is:

1. **Railway** — Web/API/database services; connect to external LiveKit with supported UDP/TURN media connectivity.
2. **AWS** — Cloud-native services, managed PostgreSQL/object storage/secrets, media networking and observability.
3. **Azure** — Equivalent application deployment with managed database/storage/secrets and appropriate WebRTC networking.

See `deploy/DEPLOYMENT_SEQUENCE.md`, `deploy/railway/README.md`, `deploy/aws/README.md`, `deploy/azure/README.md` and `docs/CLOUD_DEPLOYMENT.md`. **These are plans/baselines, not proof of successful cloud deployment.**

## Security and privacy — read before publishing or deploying

**The current application is for local development. Do not expose it as a public production service yet.**

- Never commit `.env`, secrets, API keys, private user data or uploaded files.
- Replace any local default credentials, including LiveKit, TURN and PostgreSQL credentials, before public deployment.
- Use HTTPS/WSS, hardened TURN/SFU networking and secure secret storage.
- Unify meeting admission/host permissions with verified workspace user identities; legacy host/guest keys are not an adequate enterprise authorization model.
- Replace development browser-token handling with production-grade session/cookie handling, CSRF protection, login rate limiting and suitable account recovery.
- Add audit logging, file-type/size validation, malware scanning, tenant isolation, backups and retention policies.
- Upgrade dependencies as security advisories are released and run dependency/security checks.

See `docs/DELIVERY_STATUS.md` for implementation gaps.

## Repository layout

```text
SignalRoom/
├── frontend/          # Next.js + React app, meeting UI and tests
├── backend/           # Java Spring Boot REST API and JUnit tests
├── infrastructure/    # LiveKit and coturn development configuration
├── deploy/            # Railway, AWS and Azure plans
├── docs/              # Delivery status, networking and capacity plans
├── scripts/           # Smoke checks
├── docker-compose.yml # Local multi-service stack
└── .env.example       # Local-only configuration template
```

## Roadmap / outstanding work

- Complete private/group messaging, notifications, presence, threads/reactions and search.
- Complete meeting UX (speaker/gallery modes, robust moderation, recordings/captions and reconnection).
- Integrate identities and permissions consistently across all features.
- Add object storage, file scanning and production-grade persistence.
- Implement scheduling invitations/reminders, SSO, audit trails and tenant isolation.
- Validate TURN connectivity and conduct realistic 60-participant media load tests.
- Complete production infrastructure, observability, backup/recovery and security acceptance testing.

## Contributing and licensing

Issues and pull requests are welcome once a public repository and contribution policy are established. **No open-source license is granted by this README alone.** If you intend to permit reuse, add a `LICENSE` file (e.g. Apache-2.0 or MIT after reviewing third-party obligations). Brand names belong to their respective owners.

---

**Project:** SignalRoom · **Status:** Active development · **Primary stack:** Next.js, Java, Spring Boot, LiveKit, PostgreSQL, Redis, Docker
