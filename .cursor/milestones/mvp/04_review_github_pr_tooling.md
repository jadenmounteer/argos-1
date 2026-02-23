# Review Github PR tooling.

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

[ ] Task 2 The "PR Intelligence Bridge"
Goal: Create a direct pipeline where ARGOS-1 can fetch a PR and immediately process it against your local directives.

Implement GitHubService: Create a Spring Boot service using org.kohsuke:github-api that accepts a PR ID, fetches the diff string, and identifies which files were changed.

Create the "Review Controller": Build a simple endpoint (e.g., /api/review/{prId}) that triggers the following sequence:

Fetch: Pull the PR diff from GitHub.

Ground: Read the .argos/directives Markdown files.

Analyze: Send the combined [Directives] + [Diff] to your LLM Kernel.

Senior Note: To save tokens and avoid the "clutter" of massive diffs, only fetch the diff for the initial scan. If the LLM identifies a specific file as "suspicious," you can then fetch the full content of that single file.

[ ] Task 3: Diff Parsing & Cleaning
Implement a "Diff Sanitizer" utility to strip out non-essential metadata (like hunk headers @@ -1,4 +1,4 @@) to save LLM tokens.

Create a logic gate that prevents ARGOS from fetching binary files (images, PDFs) or massive generated files (like package-lock.json).

[ ] Task 5: Tactical Status Updates
Update the React Console to show what is going on and at what stage in the process you are in. This might already work because of our thought logic.
