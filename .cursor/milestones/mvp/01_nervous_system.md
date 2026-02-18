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

#### [x] Task 3: Domain-Driven Kernel Design

Create a KernelController (The Interface) and an IntelligenceService (The Core).

Implement an AgentResponse DTO to handle structured communication between the backend and (future) frontend.

Stick to a port and adapter (hexagonal) flavor of DDD from the start.

Recommended layout:
com.argos.interface.rest: Put your KernelController here. This is the "Adapter." It only knows about JSON and HTTP.

com.argos.domain.model: Put your IntelligenceService and your ArgosCommand (Value Object) here. This is the "Heart" of the system.

com.argos.domain.ports: Put your IntelligencePort (Interface) here.

#### [x] Task 4: The Argos-1 Persona Integration

Configure a SystemPromptProvider that injects the ARGOS-1 personality into every LLM call.

Implement a "Sanity Check" tool that tests if the LLM correctly identifies itself as ARGOS-1.

Instead of a standard REST route, implement this as a custom HealthIndicator or a dedicated /actuator/identity endpoint.

#### [ ] Task 5: JSON-RPC over SSE Bridge (Intelligence Gateway)

Endpoint: Implement POST /api/v1/command as the primary entry point for agentic interactions.

Transport Layer: Configure the response as a Server-Sent Events (SSE) stream (text/event-stream) to allow real-time "Thinking" logs.

Protocol Structure: Use a JSON-RPC 2.0-aligned body (id, method, params) to ensure forward compatibility with the MCP (Model Context Protocol).

Event Types: Implement three distinct event types to be consumed by the Tactical Console:

thought: Incremental tokens inside the <thought> block from DeepSeek-R1.

action: Notifications when the agent prepares or executes a tool call.

response: The final synthesized output for the user.

Safety: Implement an idle-timeout and connection-cleanup strategy to prevent memory leaks in the Spring Boot Kernel.

Make sure to place the JSON-RPC bridge in an application layer, not the domain.

#### [ ] Task 6: Include Integration Tests to verify the connection between Spring Boot and the local Ollama API.

Use WireMock for your standard integration tests, but structure your code so you can toggle a "Live Smoke Test" profile when you want to verify the real model.
