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
	@echo "  test    Run JUnit (backend) and frontend tests"
	@echo "  down    Shut down all systems"
	@echo "  logs    Tail the system logs"

setup:
	@echo "Initializing ARGOS-1..."
	ollama pull deepseek-r1:8b
	cd backend && mvn clean install
	@if [ -d frontend ]; then cd frontend && npm install; fi

run-backend:
	cd backend && mvn clean install && mvn spring-boot:run

run-frontend:
	cd frontend && npm ci && npm run dev

test:
	cd backend && mvn test
	@if [ -d frontend ]; then cd frontend && npm test; fi

    
