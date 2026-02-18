# Directive Protocol

## Goal

Implement the file-system "Sensor" that reads architectural rules from the .argos/directives folder and injects them into the LLM context.

These rules can be shared by Cursor by having the .cursorRules reference the directives.

This is what separates a generic chatbot from an Architectural argos. This protocol ensures that ARGOS-1 uses your local .argos/directives folder as its "Prime Directive," grounding its reasoning in your specific domain rules rather than generic training data.

Goal: Create a high-fidelity context injection system that reads local Markdown files and uses them to ground the LLM's architectural analysis.

### Tasks

Note on Token Limits: Local models (8B) have smaller context windows than GPT-4. If your directives are 50 pages long, you will "clog" the brain. Keep directives concise and "snackable."

Note on Security: Ensure the DirectiveScanner is restricted to only read from the .argos folder to prevent "Prompt Injection" attacks where a user asks ARGOS to read sensitive system files.

[ ] Task 1: Directive Schema & Storage
Create the .argos/directives/ directory structure at the project root.

Populate it with initial "Mission Parameters" (e.g., architecture.md, security.md, coding_standards.md).

Senior Note: Ensure these files are written in clear, imperative Markdown (e.g., "The system SHALL NOT allow direct DB access from the UI layer").

[ ] Task 2: The "Directive Scanner" Service
Implement a Java service in the Spring Boot Kernel to perform an IO-safe scan of the .argos/directives folder.

Add a caching layer using a simple Map or Caffeine so the system doesn't hit the disk for every single token generation.

[ ] Task 3: Semantic Router (The Logic)
Implement logic that allows ARGOS-1 to decide which directive is relevant to a query.

MVP Approach: A "keyword-based" trigger (e.g., if the user says "security," ARGOS fetches security.md).

Advanced Approach: A "System Directive" that always prepends the most critical rules to every prompt.

[ ] Task 4: Prompt Engineering & Grounding
Modify the IntelligenceService to wrap the user's query in a "Grounding Template."

Template Structure: [CONTEXT: Directives] -> [USER QUERY] -> [PERSONALITY CONSTRAINTS].

Ensure the LLM cites which directive it is following (e.g., "Per Directive 1.2, I recommend moving this logic...").

[ ] Task 5: Directory Watcher (Live Updates)
Implement a WatchService in Java that detects when you change a Markdown file in the .argos/directives folder.

Log a "Sensor Update" to the Tactical Console when a directive is modified, showing ARGOS-1 is "Re-calibrating."

#### [ ] Task 6: Include Unit Tests for the DirectiveScanner to ensure it handles missing files or malformed Markdown gracefully.
