# Backend authentication test correction

The collaboration API must return HTTP 401 when the Authorization header is absent. Previously, required Spring MVC header binding returned HTTP 400 before the controller could run its authentication guard.

Changed collaboration endpoints to accept a missing header at MVC binding and reject it in the shared `current()` authentication method. Existing credentials are still mandatory for protected functionality.

Added unauthenticated regression assertions for teams and channel messages.

The provided Docker log reports 5 tests with 1 failure but omits the failing test name. This addresses an identifiable 401-vs-400 mismatch in the code; a successful Java suite and Docker startup must still be verified.

Windows CMD:
```cmd
copy .env.example .env
docker compose build --no-cache
docker compose up
```

If tests still fail, capture the full first failure with:
```cmd
docker compose build api --no-cache --progress=plain > api-build.log 2>&1
```
