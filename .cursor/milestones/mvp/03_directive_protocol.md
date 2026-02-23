Gemini said
Here is the updated Milestone 03. I’ve refined Task 4 to ensure the "Brain" of the operation lives in the ArgosSystemPromptProvider, keeping your CommandGateway focused on orchestration.

Milestone 03: The Directive Protocol & Unified Voice Gateway
Goal
Implement the file-system "Sensor" and an intelligent Gateway that orchestrates local directives and GitHub data. This allows ARGOS-1 to pivot from a general assistant to a "Grounded Architect" via a UI toggle or natural language voice commands like "Review PR 101."

[x] Task 1: Directive Schema & Storage
Goal: Establish the physical source of truth for architectural rules.

[x] Create .argos/directives/ directory at the project root.

[x] Create architecture.md, api_standards.md

[x] Task 2: The "Directive Scanner" Service
Goal: A robust Java service to pull local Markdown into the kernel.

[x] Create DirectiveScannerService.java.

[x] Implement getCombinedDirectives(): Reads all .md files in the configured path and returns a single concatenated String.

[x] Add a Map<String, String> cache to minimize disk I/O during the demo.

[] Task 3: The GitHub Adapter (FDE Fast-Path)
Goal: Connect to GitHub to fetch the "Subject Material" for reviews.

[ ] Add org.kohsuke:github-api to pom.xml.

[ ] Implement GitHubService.java:

Use ${GITHUB_TOKEN} and ${TARGET_GITHUB_REPO} from environment variables.

Implement getRawDiff(int prId): Fetches the raw diff text from GitHub's .diff URL via RestTemplate.

[ ] Task 4: Unified Gateway & "Dynamic Assembly" Prompting
Goal: The "Brain" that intercepts voice/text prompts and hydrates the system prompt using decoupled Ports and a task-specific persona shift.

[ ] Define Domain Ports (Interfaces):

Create DirectivePort.java with method String getCombinedDirectives().

Create GitRepoPort.java with method String getRawDiff(int prId).

[ ] Implement Infrastructure Adapters:

Ensure DirectiveScannerService implements DirectivePort.

Ensure GitHubService implements GitRepoPort.

[ ] Refactor JsonRpcParams:

Add boolean isGrounded, String internalContext, and String directives.

Strict Rule: Annotate internalContext and directives with @JsonIgnore to ensure they remain Server-Side Only and cannot be manipulated by the client.

[ ] Update CommandGateway.java (Orchestration):

Dependency Inversion: Inject DirectivePort and GitRepoPort interfaces via constructor.

Voice-Intent Detection: Implement Regex: (?i)(?:review|analyze|check|scan)\s+(?:pull\s+request|pr)\s+(?:number\s+)?(\d+).

Thought Injection: Before executing I/O, push status updates to the TokenHandler (e.g., tokenHandler.handleThought("🛰️ COMMAND DETECTED: Fetching PR Data...")).

Hydration Logic:

If Regex matches: Fetch diff via GitRepoPort into internalContext.

If isGrounded is true OR PR is detected: Fetch directives via DirectivePort.

[ ] Update ArgosSystemPromptProvider.java (The Persona Shift):

Layered Assembly: Modify getSystemPrompt(JsonRpcParams params) to build the prompt in stages:

Base: Standard Argos-1 persona.

Grounding (If directives present): Append rules and instructions to prioritize local architecture standards.

Tactical (If internalContext present): Append the "Senior Architect" persona instructions. Explicitly require File Paths and Line Numbers for all findings.

[ ] Task 5: Frontend "Tactical Toggle" & Feedback (React)
Goal: Visual confirmation that the voice command was understood.

[ ] Add Toggle Component: Switch labeled "Use local directives". Ensure it sends isGrounded: true in the API payload when active.

[ ] Visual Feedback Loop:

When isGrounded is active, change the chat input border to a "Pioneer Blue" glow.

When a PR command is detected, display a temporary "Tactical HUD" badge: 🛰️ COMMAND: PR_REVIEW_ACTIVE.

[ ] Status Stream: Update the UI to show sub-stages (e.g., "🔍 Scanning local directives...") to keep the user engaged during I/O.

Senior Dev Implementation Note for Cursor:
"Cursor, in CommandGateway.java, use the regex (?i)(?:review|analyze|check|scan)\s+(?:pull\s+request|pr)\s+(?:number\s+)?(\d+) to detect PR intents. Crucially, do not modify the streaming flow in IntelligenceService. Instead, move the 'Grounding' logic into ArgosSystemPromptProvider. This ensures that CommandGateway hydrates the data, and SystemPromptProvider assembles the final instructions for the LlmStreamPort."

The "Architect" Defense for Task 4:
When you demo this, you’re showing Separation of Concerns.

"The Gateway identifies the Intent, the Infrastructure services fetch the Context, and the Prompt Provider handles the Instruction Assembly. This keeps our streaming pipeline pure and allows us to swap models or data sources without re-writing our core logic."
