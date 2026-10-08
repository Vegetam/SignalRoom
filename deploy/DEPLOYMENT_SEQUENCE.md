# SignalRoom deployment progression

**1. Railway → 2. AWS → 3. Azure**

These are deployment targets for the *same* Teams-style product; they are not substitutes for completing features.
Each environment must pass an identical acceptance suite. Shipping infrastructure files does not certify the product.

| Gate | Railway | AWS | Azure |
| --- | --- | --- | --- |
| Build frontend and backend | pending | pending | pending |
| Auth, teams, channels, files, chat, calendar | pending | pending | pending |
| Meetings, host permissions, waiting room, 60-seat enforcement | pending | pending | pending |
| External-network audio/video and TURN | pending | pending | pending |
| 60-person conferencing load test | pending | pending | pending |
| Security and isolation audit | pending | pending | pending |
| Backups, restore, monitoring, incident runbooks | pending | pending | pending |

**Known limitations:** The current source does not yet meet the requested complete Microsoft Teams feature set, nor has an independent certification been performed. See `docs/DELIVERY_STATUS.md`.
