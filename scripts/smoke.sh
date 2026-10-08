#!/usr/bin/env bash
set -euo pipefail
API_URL="${API_URL:-http://localhost:8080}"
curl --fail --silent --show-error "$API_URL/actuator/health"
printf '\n'
ROOM="smoke-$(date +%s)-$$"
CREATE_JSON=$(curl --fail --silent --show-error -H 'Content-Type: application/json' \
  -d "{\"room\":\"$ROOM\",\"displayName\":\"Smoke Host\"}" "$API_URL/api/meetings")
HOST_KEY=$(printf '%s' "$CREATE_JSON" | python3 -c 'import sys,json; print(json.load(sys.stdin)["hostKey"])')
JOIN_JSON=$(curl --fail --silent --show-error -H 'Content-Type: application/json' \
  -d "{\"room\":\"$ROOM\",\"accessKey\":\"$HOST_KEY\"}" "$API_URL/api/join")
printf '%s' "$JOIN_JSON" | python3 -c 'import sys,json; j=json.load(sys.stdin); assert j["token"] and j["room"] and j["role"] == "HOST"; print("join token: OK")'
