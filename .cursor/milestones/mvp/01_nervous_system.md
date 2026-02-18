# The Nervous System

## Goal

Establish the Spring Boot backend, connect it to the local Ollama LLM, and verify the "Reasoning" loop.

By the end of this milestone, ARGOS-1 will have thought and reasoned for the first time.

### Tasks

#### [x] Task 1: Environment Initialization (Ollama)

Install Ollama and pull the deepseek-r1:8b model (or llama3.1:8b if hardware prefers).

Note on Model Selection: DeepSeek-R1 is highly recommended for this milestone because it uses <thought> tags. We can parse these tags in the Kernel to show the user what ARGOS is "thinking" before it speaks.

Verify the local API is reachable at http://localhost:11434/api/generate.

I verified with this curl:

```
curl http://localhost:11434/api/generate -d '{
"model": "deepseek-r1:8b",
"prompt": "Why is the sky blue?",
"stream": false
}'
```

#### [x] Task 2: Spring Boot Kernel Setup

Initialize a Spring Boot 3.3+ project with Java 21.

Add dependencies: spring-boot-starter-web, lombok, and langchain4j-ollama-spring-boot-starter.

Enable Virtual Threads in application.properties: spring.threads.virtual.enabled=true.

#### [ ] Task 3: Domain-Driven Kernel Design

Create a KernelController (The Interface) and an IntelligenceService (The Core).

Implement an AgentResponse DTO to handle structured communication between the backend and (future) frontend.

#### [ ] Task 4: The "Starfleet" Persona Integration

Configure a SystemPromptProvider that injects the ARGOS-1 personality into every LLM call.

Implement a "Sanity Check" tool that tests if the LLM correctly identifies itself as ARGOS-1.

#### [ ] Task 5: JSON-RPC Bridge Setup (Foundation for MCP)

Create a basic endpoint /api/v1/command that accepts text input and returns the LLM's "Thought" and "Action" stream.

#### [ ] Task 6: Include Integration Tests to verify the connection between Spring Boot and the local Ollama API.

[ ] Task 7: Containerized Orchestration (Docker Compose)
Create a docker-compose.yml that links the Spring Boot Hub, the React Console, and Ollama.

Use a Named Volume for Ollama (e.g., ollama_data:/root/.ollama) so models aren't re-downloaded when the container restarts.

Senior Choice: Keep Ollama in its own container to demonstrate that the "Brain" is an interchangeable service.

```

```
