# ARGOS-1 Command Center
# Monorepo: backend/ (Spring Boot), frontend/ (React — when present)

.PHONY: help setup run build test up down logs

help:
	@echo "ARGOS-1: Architectural Review & Governance Orchestration System"
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  setup   Install dependencies and pull AI models (backend + frontend if present)"
	@echo "  run     Run the Command Hub locally (Spring Boot on port 8081; requires Ollama)"
	@echo "  build   Rebuild Java and React containers (Docker)"
	@echo "  test    Run JUnit (backend) and frontend tests"
	@echo "  up      Start the entire ship (Docker Compose)"
	@echo "  down    Shut down all systems"
	@echo "  logs    Tail the system logs"

setup:
	@echo "Initializing ARGOS-1..."
	ollama pull deepseek-r1:8b
	cd backend && mvn clean install
	@if [ -d frontend ]; then cd frontend && npm install; fi

run:
	cd backend && mvn spring-boot:run

build:
	docker-compose build

test:
	cd backend && mvn test
	@if [ -d frontend ]; then cd frontend && npm test; fi

up:
	docker-compose up -d

down:
	docker-compose down

logs:
	docker-compose logs -f