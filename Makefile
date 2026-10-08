.PHONY: up down test smoke
up:
	docker compose up --build
down:
	docker compose down
test:
	cd backend && mvn test
	cd frontend && npm test
smoke:
	bash scripts/smoke.sh
