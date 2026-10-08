#!/usr/bin/env bash
set -euo pipefail
curl --fail --silent http://localhost:8080/actuator/health
printf '\n'
curl --fail --silent -H 'Content-Type: application/json' -d '{"room":"smoke-test","displayName":"Smoke"}' http://localhost:8080/api/join | python3 -c 'import sys,json; j=json.load(sys.stdin); assert j["token"] and j["room"]=="smoke-test"; print("join token: OK")'
