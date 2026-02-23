Goal
Implement the file-system "Sensor" that reads local architectural rules from the .argos/directives folder and injects them into the LLM context. This ensures ARGOS-1 reasons based on project-specific rules rather than generic AI training data.

[ ] Task 1: Directive Schema & Storage
Goal: Establish the physical source of truth for architectural rules.

[ ] Create .argos/directives/ directory at the project root.

[ ] Create architecture.md: Define rules for Java/Spring Boot (e.g., "Use Repository pattern for DB access").

[ ] Create api_standards.md: Define GraphQL rules (e.g., "All queries must use DataLoaders to avoid N+1 issues").

[ ] Create fde_prototyping.md: Define speed rules (e.g., "Prioritize Tailwind for rapid UI iteration").

[ ] Task 2: The "Directive Scanner" Service
Goal: A safe, robust Java service to pull local Markdown into the kernel.

[ ] Create DirectiveScannerService.java in the kernel.

[ ] Implement loadAllDirectives(): Use java.nio.file to read all .md files in the configured path.

[ ] Use @Value("${argos.directives.path}") to inject the folder path from application.properties.

[ ] Add a simple Map<String, String> cache to store file contents so the kernel isn't hitting the disk on every prompt.

[ ] Task 3: The PR Intelligence Bridge (Merged GitHub Logic)
Goal: Connect to the GitHub API to fetch the "Subject Material" for the review.

[ ] Add org.kohsuke:github-api dependency to pom.xml.

[ ] Implement GitHubService.java: Use @Value("${github.auth.token}") and @Value("${github.target.repo}").

[ ] Implement getPrDiff(int prId): Fetch the raw diff from the PR.

[ ] FDE Shortcut: Use restTemplate to fetch the diffUrl content as a raw string to save on complex parsing.

Gemini said
To show the VP of Architecture that you understand the "Cost vs. Context" trade-off, we’ll restructure Task 4 to support Conditional Context. This proves you’re building a production tool, not just a script.

Adding a toggle to the React frontend is a great "visual" win for the demo—it makes the "AI Grounding" concept tangible for non-technical folks like Tania.

[ ] Task 4: Context-Aware Intelligence Integration
Goal: Implement "Selective Hydration" of the LLM context so ARGOS-1 can switch between a generic coding assistant and a project-specific Senior Architect.

Backend: Intelligence Service Logic
[ ] Refactor IntelligenceService.java: Create a unified ChatRequest DTO that includes the user prompt and a boolean isGrounded.

[ ] Implement Conditional Prompting:

If isGrounded is False: Send the raw user prompt.

If isGrounded is True: Wrap the user prompt in the Architectural Template (Injecting the directives from Task 2).

[ ] Logic for PR Reviews: Ensure the /api/tactical/review endpoint always sets isGrounded = true by default, as a PR review without rules is just a syntax check.

Frontend: The "Tactical Toggle" (React)
[ ] Add UI Component: In the chat input area, add a stylized Toggle Switch labeled "Use local directives"

[ ] State Management: Connect the toggle state to your API call. When the toggle is "Active," the frontend sends isGrounded: true to the backend.

[ ] Visual Feedback: When Grounding is active, change the border color of the chat input (e.g., to a "Pioneer Blue" or "Tactical Amber") to signify that the AI is now reasoning against local directives.

[ ] Task 5: The Tactical Review Endpoint
Goal: Create the trigger mechanism for the demo.

[ ] Implement ReviewController.java with a GET mapping: /api/tactical/review/{prId}.

[ ] Orchestrate the flow: Scanner -> GitHub -> Intelligence -> Response.

[ ] Log "Tactical Scan Initiated" to the console.

Senior Dev Implementation Note for Cursor:
"Cursor, when implementing the DirectiveScannerService, ensure you handle the IOException if the .argos folder is missing. If it is missing, log a warning but do not crash the application; simply proceed with an empty directive set."
