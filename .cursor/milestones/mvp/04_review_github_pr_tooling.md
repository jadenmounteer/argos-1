Goal
Establish a high-bandwidth connection to GitHub and implement "Intelligence Filtering" to ensure the LLM receives clean, relevant code data. Optimize for a voice-activated demo where "Review PR 101" triggers an automated architectural scan.

[ ] Task 1: GitHub Infrastructure & Voice-Intent Detection
Goal: Secure the link and make the system "listen" for tactical commands.

[ ] Client Initialization: In GitHubService.java, initialize the GitHub client using ${GITHUB_TOKEN} and add a @PostConstruct health check.

[ ] Voice-Intent Router: In CommandGateway.java, implement the "Smart Interceptor":

Use Regex: (?i)(?:review|analyze|check|scan)\s+(?:pull\s+request|pr)\s+(?:number\s+)?(\d+).

If a match is found, extract the prId and automatically set isGrounded = true.

[ ] Resilience: Implement TacticalLinkException to handle cases where the PR ID is misspoken or the repo is unreachable.

[ ] Task 2: Advanced Diff Extraction (The "FDE" Shortcut)
Goal: Fetch the PR data quickly and store it in the internalContext.

[ ] Implement fetchRawDiff: Use GHPullRequest.getDiffUrl() and RestTemplate to grab the raw string.

[ ] Context Injection: Pass the raw diff into the ChatRequestDTO's internalContext field so the IntelligenceService can append it to the prompt.

[ ] Task 3: The "Noise Filter" & Token Guard
Goal: Clean the data to prevent "Clogged Brain" and save on LLM latency.

[ ] Implement DiffSanitizer.java:

Skip binary files (images, .jar, .exe).

Ignore lockfiles and metadata: package-lock.json, pnpm-lock.yaml, .gitignore, .DS_Store, target/.

Hunk Header Logic: Keep @@ headers for small diffs (helps with line numbers), but strip them if the diff exceeds 5,000 characters.

[ ] Size Guard: If the sanitized diff > 20,000 characters, truncate and append a warning: [TRUNCATED: PR too large for full scan].

[ ] Task 4: Real-Time Tactical Feedback (React)
Goal: Make the "Invisible" voice process visible and professional.

[ ] Prompt Engineering: Update the SystemPrompt to force a "Senior Architect" persona that provides File Path and Line Number for every violation.

[ ] React Markdown: Ensure the output renders as a professional report using react-markdown.

Senior Dev Implementation Note for Cursor:
"Cursor, when implementing the DiffSanitizer, use a case-insensitive Regex to identify and skip common metadata files. In the CommandGateway, ensure that the voice-intent detection happens BEFORE the LLM call, so that the diff is already present in the prompt context when the model starts generating."
