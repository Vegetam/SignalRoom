# Delivery status — scope versus implementation

| Product area | Source implementation | Verification remaining |
|---|---|---|
| Registration / login | Local user records, salted PBKDF2 password hashes, 7-day opaque sessions, logout | Java integration suite and security audit |
| Teams / channels | Create teams and channels, membership administration, list members | Multi-user browser test |
| Persistent channel chat | HTTP send/read with PostgreSQL storage, 2.5-second polling | Multi-user browser test and pagination at scale |
| File sharing | Member-only upload/download, local Docker volume, 15 MB maximum | Browser and antivirus checks; cloud object storage |
| Calendar | Create/list personal events; attach a meeting room ID | Notifications, shared calendars, timezone handling |
| Conference UI | LiveKit React conference component, gallery, screen share and controls | Full UI/AV functional testing |
| Waiting rooms | Host approval / denial before a room token is issued | Browser and concurrent admission tests |
| Meeting history | Meeting database history tied to bearer-like host key in browser session | Tie history to authenticated accounts |
| 60 participants | LiveKit room max=60; Java admission cap reserves one host seat | Load/soak test of 60 real concurrent peers, media quality measurements |
| WebRTC inspector | Browser-accessible session stats and SDP event tapping | Packet-level SFU side telemetry and controlled network tests |
| End meeting | SFU DeleteRoom request followed by DB status update | Integrated SFU test |
| Cloud | Compose architecture and deployment guidance | TLS, TURN integration, scalability, security audit, operations |

## Explicitly incomplete parity features

Direct/private messaging, threads, message search, file previews/versioning, channel owners editing/removal, rich meeting scheduling invites, email/push notifications, fully authenticated meeting ownership, enterprise SSO/SCIM, speaker pinning/spotlighting, meeting recording/playback, transcripts/captions, breakout rooms, virtual backgrounds, advanced admin console, audit export, distributed chat fanout, true multi-tenant isolation, 60-user performance validation, production-grade TURN/TLS.

Do not characterize these items as delivered. A complete Teams clone is a multi-release product, and these are material gaps.
