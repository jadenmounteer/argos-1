# ARGOS-1 Command Center

.PHONY: help setup up down build test logs

help:
	@echo "ARGOS-1: Architectural Review & Governance Orchestration System"
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  setup   Install dependencies and pull AI models"
	@echo "  up      Start the entire ship (Docker Compose)"
	@echo "  down    Shut down all systems"
	@echo "  build   Rebuild Java and React containers"
	@echo "  test    Run the full suite of JUnit and ArchUnit tests"
	@echo "  logs    Tail the system logs"

setup:
	@echo "Initializing ARGOS-1..."
	ollama pull deepseek-r1:8b
	mvn clean install
	cd frontend && npm install

up:
	docker-compose up -d

down:
	docker-compose down

test:
	mvn test
	cd frontend && npm test

logs:
	docker-compose logs -f