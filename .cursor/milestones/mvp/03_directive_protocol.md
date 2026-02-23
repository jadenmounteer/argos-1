Goal
Implement the file-system "Sensor" and an intelligent Gateway that orchestrates local directives and GitHub data. This allows ARGOS-1 to pivot from a general assistant to a "Grounded Architect" via a UI toggle or natural language voice commands like "Review PR 101."

[ ] Task 1: Directive Schema & Storage
Goal: Establish the physical source of truth for architectural rules.

[ ] Create .argos/directives/ directory at the project root.

[ ] Create architecture.md, api_standards.md, and fde_prototyping.md.

[ ] Senior Note: Write 2-3 high-impact rules in each (e.g., "All GraphQL resolvers MUST use the @BatchMapping pattern to prevent N+1 queries").

[ ] Task 2: The "Directive Scanner" Service
Goal: A robust Java service to pull local Markdown into the kernel.

[ ] Create DirectiveScannerService.java.

[ ] Implement getCombinedDirectives(): Reads all .md files in the configured path and returns a single concatenated String.

[ ] Add a Map<String, String> cache to minimize disk I/O during the demo.

[ ] Task 3: The GitHub Adapter (FDE Fast-Path)
Goal: Connect to GitHub to fetch the "Subject Material" for reviews.

[ ] Add org.kohsuke:github-api to pom.xml.

[ ] Implement GitHubService.java:

Use ${GITHUB_TOKEN} and ${GITHUB_REPO} from environment variables.

Implement getRawDiff(int prId): Fetches the raw diff text from GitHub's .diff URL via RestTemplate.

[ ] Task 4: Unified Gateway & Voice-Intent Detection
Goal: The "Brain" that intercepts voice prompts to hydrate context.

[ ] Refactor ChatRequestDTO: Add boolean isGrounded and String internalContext.

[ ] Update CommandGateway.java:

Voice-Intent Logic: Use Regex to check the incoming message for phrases like "review PR [ID]" or "analyze PR [ID]".

Hydration: If a PR is detected, call GitHubService.getRawDiff() and store it in internalContext.

Grounding: If isGrounded is true OR a PR is detected, call DirectiveScannerService to fetch the architectural rules.

[ ] Update IntelligenceService: Wrap the user's voice prompt in a Grounding Template if directives or context are present.

[ ] Task 5: Frontend "Tactical Toggle" & Feedback (React)
Goal: Visual confirmation that the voice command was understood.

[ ] Add Toggle Component: Switch labeled "Use local directives". Ensure it sends isGrounded: true in the API payload when active.

[ ] Visual Feedback Loop:

When isGrounded is active, change the chat input border to a "Pioneer Blue" glow.

When a PR command is detected, display a temporary "Tactical HUD" badge: 🛰️ COMMAND: PR_REVIEW_ACTIVE.

[ ] Status Stream: Ensure the UI displays the sub-stages (e.g., "🔍 Scanning local directives...") so the user knows why there is a slight pause for I/O.

Senior Dev Implementation Note for Cursor:
"Cursor, in CommandGateway.java, use the following regex to detect PR intents in the voice string: (?i)(?:review|analyze|check|scan)\s+(?:pull\s+request|pr)\s+(?:number\s+)?(\d+). If a match is found, capture group 1 as the prId and automatically set isGrounded = true for the duration of that request."
