# Roadmap

1. AuthN/AuthZ: OIDC (Keycloak), secure invitation links, room host/moderation roles, short-lived tokens, rate limiting, audit.
2. Network resilience: cert-managed HTTPS/WSS, production TURN integration, ICE restart / relay-only E2E tests, IPv6 tests, NAT profiles.
3. Deep instrumentation: LiveKit server metrics, OpenTelemetry API traces, Prometheus/Grafana, candidate-pair history, RTCP feedback visualisation, SDP redaction-safe snapshots.
4. Media QoS: simulcast/SVC layer selection UI, packet-loss simulation, frame/RTT targets, controlled load tests, autoscaling.
5. Collaboration: persistent messaging, meeting schedule/recordings (with consent), admin dashboard, room history, permissions.
6. Engineering: Testcontainers, Playwright browser E2E with two clients, cross-browser test matrix, CI, dependency scanning and SBOM.
7. Scale: Kubernetes, geographically distributed SFUs, leader election and placement, disruption recovery and recording workers.
