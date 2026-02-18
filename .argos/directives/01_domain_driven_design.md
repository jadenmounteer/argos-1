Directive: Strict Bounded Contexts and Ubiquitous Language.

Rules:

Business logic MUST reside in the domain package.

The infrastructure package is for external concerns (DB, API clients, Ollama). It must never leak into the domain.

Use Value Objects for data that doesn't have an identity (e.g., CommandTranscript).

Use Services only for logic that doesn't naturally fit into an Entity.
