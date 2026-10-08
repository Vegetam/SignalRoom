# SignalRoom — Teams-style collaboration platform

This repository contains a Next.js/React user application, a Java 21/Spring Boot API, PostgreSQL, Redis, LiveKit SFU, and coturn. The UI contains accounts, teams/channels, team membership, persisted channel chat, file sharing, calendar events, and a meeting UI with host-controlled waiting rooms and a 60-person admission/SFU cap.

**This is a substantial source implementation, NOT a verified production-ready full Teams clone.** Full Zoom/Teams feature parity also requires capabilities not yet implemented here: one-to-one/group private chat, presence, calls from contacts, threaded/reaction messages, cloud object storage, event invites/reminders, SSO, recordings, live captions/transcription, background blur, mobile clients, tenant isolation, backups, compliance workflows and 60-client load-test evidence. See `docs/DELIVERY_STATUS.md`.

## Windows Docker Desktop — first run

1. Install and start Docker Desktop, ensure Linux containers are selected.
2. Extract the ZIP. Open **Command Prompt (CMD)** in the directory containing `docker-compose.yml`.
3. Execute:

```bat
copy .env.example .env
docker compose config
docker compose up --build
```

4. Visit **http://localhost:3000**. Register an account (password minimum 10 characters). Create a team and channel, then send messages. Open the Meet tab to create a meeting.
5. To allow another local user to join, use the meeting ID in the meeting lobby and approve the person from the host's waiting-room controls.

Useful commands (Windows CMD):

```bat
docker compose ps
docker compose logs --tail=150 api
docker compose logs --tail=150 web
docker compose logs --tail=150 sfu
docker compose down
```

`docker compose down -v` **deletes PostgreSQL data and uploaded files**; do not use it unless intentionally resetting the workspace.

## Important about the previous npm error

The original ZIP pinned `livekit-client@2.15.5` with `@livekit/components-react@2.9.24`, whose peer requirement is `livekit-client@^2.20.1`. This repository corrects that mismatch by pinning **livekit-client 2.20.1**. Do not use `npm --force` or `--legacy-peer-deps` to hide an invalid dependency tree. Packages need to be downloaded and installed in your environment, which can reach npm.

## Components

| Component | Address (development) | Responsibilities |
|---|---|---|
| Next.js web | http://localhost:3000 | Dashboard, teams, chat, files, calendar, meetings |
| Spring Boot API | http://localhost:8080 | Local authentication, persisted business data, meeting admissions |
| PostgreSQL | Compose internal only | Durable workspace, meeting and event data |
| LiveKit SFU | ws://localhost:7880 | ICE/DTLS-SRTP, RTP/RTCP, SFU subscriptions |
| coturn | localhost:3478 | Independent TURN service; **not yet connected to LiveKit media routing** |
| Redis | Compose internal only | LiveKit coordination |

The Java collaboration API uses salted PBKDF2-hashed passwords and opaque, server-side sessions. Tokens reside in browser localStorage for development. Production must move to secure HttpOnly cookies with CSRF protection, rate limiting, password reset and account verification/SSO.

The legacy meeting API still uses bearer-like `hostKey` and `guestKey` tickets, created independently of workspace accounts. This is an explicit **production blocker**, not a completed enterprise authentication solution. Never expose the current deployment publicly.

## Testing

```bat
cd frontend
npm install
npm test
npm run build
cd ..\backend
mvn test
cd ..
```

For the full integrated exercise, use Docker Compose and verify an account registration, two users joining the same team, member-only access to chat/files, calendar persistence, waiting-room admission, and 60-seat admission rejection. Load testing for 60 concurrent media publishers/subscribers remains outstanding.

## Cloud deployment

`docs/CLOUD_DEPLOYMENT.md` documents cloud prerequisites, HTTPS/WSS edge routing, NAT/UDP, production secret management, and scaling. **This is deployment guidance, not a certified production deployment.** A public service needs identity/authorization hardening and end-to-end verification before release.

## Project structure

- `frontend/app/dashboard` — integrated Teams-style workspace UI.
- `frontend/app/meet` — create/join meeting lobby.
- `frontend/app/room/[roomId]` — media conference, host waiting-room moderation, inspector.
- `backend/src/main/java/com/example/meet/CollaborationController.java` — user/team/channel/message/file/event endpoints.
- `backend/src/main/java/com/example/meet/MeetingController.java` — meeting tickets and admission.
- `backend/src/test` — existing media API tests and collaboration tests.
- `infrastructure` — LiveKit and coturn config.
- `docs/DELIVERY_STATUS.md` — precise implementation status and limitations.

## Three cloud targets

Delivery order: **Railway, then AWS, then Azure**. Follow `deploy/DEPLOYMENT_SEQUENCE.md`
and `deploy/railway/README.md` for the first deployment. Files under `deploy/aws/`
and `deploy/azure/` are architecture plans, not production deployment templates.
