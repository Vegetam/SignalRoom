# Public cloud architecture — preparation checklist

**Do not put the development Compose file directly on the public internet.** It uses known development LiveKit keys and a host-key meeting API disconnected from workspace identity. Production readiness requires security and infrastructure work.

Recommended initial deployment:

1. Linux VMs or Kubernetes for app/API with a managed PostgreSQL database, durable object storage, managed Redis, and HTTPS/WSS ingress.
2. Domains: `app.example.com` (Next.js), `api.example.com` (Spring Boot), `rtc.example.com` (LiveKit WebSocket); all require valid TLS certificates.
3. LiveKit requires publicly reachable UDP media ports and a correct public node IP, plus TURN/TLS for restrictive networks. A reverse proxy alone is not sufficient to carry WebRTC UDP.
4. Place LiveKit API keys, TURN credentials, database passwords and session secrets in a secret manager; rotate keys and remove all hardcoded development credentials.
5. Set frontend build args `NEXT_PUBLIC_API_URL=https://api.example.com`, `NEXT_PUBLIC_LIVEKIT_URL=wss://rtc.example.com` and backend `LIVEKIT_PUBLIC_URL=wss://rtc.example.com`, `ALLOWED_ORIGIN=https://app.example.com`.
6. Replace `localhost` LiveKit addresses and development `--node-ip 127.0.0.1` with public network configuration; validate ICE connectivity from remote networks.
7. Bind meeting creation, host permissions and histories to verified user sessions; add session revocation, CSRF defenses (if switching to cookies), rate limiting, user verification and password recovery.
8. Add a backup/restore procedure for SQL and objects, monitoring and audit trails; implement log redaction and retention controls.
9. Perform browser tests across Windows/macOS/mobile and network conditions. Validate 60 concurrent participants under controlled loss, jitter and bandwidth variation, and record actual capacity/quality evidence.
10. For production conferencing, evaluate multiple SFU nodes, regional routing, recording infrastructure and TURN availability zones.

Neither this checklist nor the included Compose automatically establishes production readiness. Cloud topology and infrastructure-as-code should be finalized for your hosting provider before deployment.
