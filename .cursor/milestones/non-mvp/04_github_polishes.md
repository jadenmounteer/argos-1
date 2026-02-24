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

[ ] Task: Implement Hybrid Voice-Intent Parser
Goal: Ensure the system correctly identifies PR IDs whether they are spoken as digits, words, or include a hashtag.

1. Frontend: Regex & Visual Sync
   [ ] Update PR_INTENT_REGEX in App.tsx (or your React Voice component):

New Regex: /(?:review|analyze|check|scan)\s+(?:pull\s*request|pr)\s*(?:number\s+)?#?(\d+|one|two|three|four|five|six|seven|eight|nine|ten)/i

Why: This adds #? for the hashtag and the word group (one|two|...) to the capture group.

2. Backend: The "Semantic Resolver"
   [ ] Update CommandGateway.java Regex:

Sync the backend Regex to match the frontend exactly to ensure the "Brain" and "Eyes" are seeing the same thing.

[ ] Implement resolvePrId Helper:

Create a method to handle the string-to-int conversion:

```
private int resolvePrId(String capturedValue) {
    Map<String, Integer> wordToNum = Map.of(
        "one", 1, "two", 2, "three", 3, "four", 4, "five", 5,
        "six", 6, "seven", 7, "eight", 8, "nine", 9, "ten", 10
    );
    String val = capturedValue.toLowerCase();
    return wordToNum.containsKey(val) ? wordToNum.get(val) : Integer.parseInt(val);
}
```

Ensure this resolvePrId method is a private helper in the Gateway. This keeps it "internal" to the input-handling logic.
