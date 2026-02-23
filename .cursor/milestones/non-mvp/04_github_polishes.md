Goal
Establish a high-bandwidth connection to GitHub and implement "Intelligence Filtering" to ensure the LLM receives clean, relevant code data. Optimize for a voice-activated demo where "Review PR 101" triggers an automated architectural scan.

[ ] Task 1: The "Noise Filter" (DiffSanitizer)
Goal: Ensure the LLM doesn't get confused by "junk" code like lockfiles or binary data.

[ ] Implement DiffSanitizer.java:

Skip Logic: Use Regex to remove sections of the diff for package-lock.json, .gitignore, and binary files.

Hunk Management: Keep the @@ line markers if the diff is small (crucial for the "Line Number" rule), but strip them if the diff is massive to save space.

[ ] Size Guard: If the diff is still > 20,000 chars after cleaning, truncate it and add the [TRUNCATED] warning so the LLM doesn't crash the context window.

[ ] Task 2: Advanced Resilience
Goal: Handle the "Demo Gremlins" (typos and bad IDs).

[ ] TacticalLinkException: If the GitHub API returns a 404 (bad PR ID), catch it in the Gateway and send a "Thought" back: "⚠️ I couldn't find PR #[ID]. Please check the number and try again."

[ ] Task 3: The "Architect" Report UI
Goal: Move away from raw text and make the output look like a formal audit.

[ ] React Markdown Integration: Use the react-markdown library in your frontend.

[ ] Syntax Highlighting: Ensure code snippets within the AI's review are formatted correctly.

[ ] Result: When the AI says "Violation Found," it should look like a professional card, not a wall of text.
