# AWS rollout (phase 2, after Railway acceptance)

Status: **Architecture plan, not deployed**.

- Web/API: ECS Fargate or EKS; separate deployments and autoscaling.
- Database: RDS PostgreSQL with encryption, backups, and Multi-AZ where required.
- Cache: ElastiCache Redis; file storage migrated to S3 with private access and signed URLs.
- Identity: managed OIDC provider; remove development session token shortcuts.
- SFU: LiveKit Cloud initially, or dedicated EC2/EKS nodes with appropriate network load balancers and public UDP/TCP paths; configure TURN and certificates.
- Routing: HTTPS ALB for API/web; correctly provision public WSS address and ports for media.
- Secrets: AWS Secrets Manager; least-privilege IAM.
- Observability: OpenTelemetry and CloudWatch / Prometheus and alerting.

Acceptance: reproduce the Railway functional suite, 60-person media load tests, failover, persistence, recovery and security reviews before routing production traffic.
