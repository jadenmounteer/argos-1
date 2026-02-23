Milestone 04: Tactical PR Analysis & Connectivity
Goal
Establish a high-bandwidth connection to GitHub and implement "Intelligence Filtering" to ensure the LLM receives clean, relevant code data for architectural review.

[ ] Task 1: GitHub Infrastructure & Resilience
Goal: Securely initialize the GitHub client and handle connection state.

[ ] Client Initialization: In GitHubService.java, use the GitHubBuilder to instantiate the client using the ${GITHUB_TOKEN}.

[ ] Connectivity Check: Add a @PostConstruct method that performs a lightweight API call (e.g., github.getMyself()) to verify the token on startup.

[ ] Error Handling: Wrap GitHub calls in a try-catch block that throws a custom TacticalLinkException if the repo or PR is not found.

[ ] Task 2: Advanced Diff Extraction (The "FDE" Shortcut)
Goal: Retrieve the PR data in the most token-efficient format.

[ ] Implement fetchRawDiff:

Use the GHPullRequest object to get the getDiffUrl().

Use RestTemplate or WebClient to fetch that URL as a String.

Senior Note: This is faster and cleaner than iterating through every file hunk via the library.

[ ] Implement fetchFileContent: Create a helper method to fetch the entire content of a specific file path if the LLM requests more context.

[ ] Task 3: The "Noise Filter" (Diff Sanitizer)
Goal: Strip out junk data to save tokens and improve LLM focus.

[ ] Implement DiffSanitizer.java:

Remove binary file references.

Ignore lockfiles (e.g., package-lock.json, pnpm-lock.yaml, target/).

Optional: Strip hunk headers (@@ ... @@) if the diff is massive, but keep them if the diff is small (they help the LLM find line numbers).

[ ] Size Guard: If the sanitized diff exceeds a specific character limit (e.g., 20,000 characters), truncate it and append a warning: [TRUNCATED: PR too large for full scan].

When you implement the DiffSanitizer, make sure it also ignores hidden files like .DS_Store or .gitignore. These add zero value to an architectural review but eat up tokens.

[ ] Task 4: Real-Time Tactical Feedback (React)
Goal: Make the "Thinking" process visible to the user (and the interviewers).

[ ] Status Stream: Update the frontend to display the current sub-stage of the review:

🛰️ Establishing link with Github...

🔍 Scanning Argos-1 Directives...

🧠 Analyzing Architectural Alignment...

[ ] Prompt Engineering: Instruct the LLM to provide the File Path and Line Number (if available) for every violation.

[ ] React Formatting: Use a library like react-markdown to make sure the LLM’s response looks like a professional report, not just a wall of text (see below example).

⚠️ Directive Violation Detected
File: src/main/java/Service.java
Rule: api_standards.md - Rule 1.2 (N+1 Prevention)
Issue: You are calling the database inside a for loop.
Suggested Fix: Refactor this to use a DataLoader...

Senior Dev Implementation Note for Cursor:
"Cursor, when implementing the DiffSanitizer, use a Regex-based approach to identify and skip common metadata files. Ensure the GitHubService uses the repo name configured in application.properties so we can easily switch between test repos."
