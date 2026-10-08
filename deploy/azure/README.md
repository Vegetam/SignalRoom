# Azure rollout (phase 3, after AWS acceptance)

Status: **Architecture plan, not deployed**.

- Web/API: Azure Container Apps or AKS (choose based on operational needs).
- Database: Azure Database for PostgreSQL Flexible Server.
- Cache: managed Redis equivalent; Azure Blob Storage for private files.
- Identity: Microsoft Entra ID through OIDC, RBAC and verified tenant isolation.
- SFU: LiveKit Cloud initially or dedicated AKS/VMSS nodes exposed via supported public UDP/TCP networking and TURN/TLS.
- Secrets: Azure Key Vault; managed identities.
- Monitoring: Azure Monitor/Application Insights, OpenTelemetry, alarms.

Acceptance: same automated tests and 60-person networking/load suite as Railway/AWS, plus Azure failover, security, backup, and restore validation.
