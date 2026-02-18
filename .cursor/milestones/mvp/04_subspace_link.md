# Subspace Link (Github Integration)

## Goal

Connect ARGOS to the GitHub API to allow it to "scan" PR diffs and fetch remote code for analysis.

### PM Recommendation

For the MVP, we will stick to Stateless Analysis. ARGOS will fetch the PR diff and the content of the modified files. This is enough to check for architectural "Directives" without the overhead of managing a full local clone/build environment.

### Tasks

Note on Rate Limiting: While the GitHub API is free, unoptimized calls (like fetching every file in a repo) will hit limits. Always fetch the "List of Files" in a PR first, then selectively fetch content.

Note on Security: Since you are running this locally, ensure your GitHub Token is in your .gitignore. If you accidentally commit it, GitHub will revoke it automatically, but it’s a bad look for a Senior Architect demo!

[ ] Task 1: GitHub API Authentication & Config
Generate a Personal Access Token (PAT) on GitHub with repo and pull_requests scopes.

Implement a secure SecretManager service in Spring Boot to store the token (use environment variables, never hardcode).

Configure the GitHub Java library (e.g., org.kohsuke:github-api) within the Kernel.

[ ] Task 2: The "Remote Scanner" Domain Service
Build a specialized GitHubDomainService that can:

List open Pull Requests for a specific repository.

Fetch the diff of a specific PR ID.

Retrieve the full content of individual files mentioned in the diff for deeper context.

[ ] Task 3: Diff Parsing & Cleaning
Implement a "Diff Sanitizer" utility to strip out non-essential metadata (like hunk headers @@ -1,4 +1,4 @@) to save LLM tokens.

Create a logic gate that prevents ARGOS from fetching binary files (images, PDFs) or massive generated files (like package-lock.json).

[ ] Task 4: Autonomous PR Review Loop
Create a new "Agent Tool" that allows ARGOS to say: "I will now fetch the code from PR #101 to check for Directive violations."

Link the GitHub data fetcher to the Intelligence Service so the PR code is injected into the prompt alongside the relevant Directives.

[ ] Task 5: Tactical Status Updates
Update the React Console to show "Subspace Connectivity" status.

Display a "Incoming Transmission" alert when ARGOS is successfully fetching code from GitHub.
