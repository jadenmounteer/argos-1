Directive: Modern Java and Reactive-Ready Performance.

Rules:

Utilize Virtual Threads for all I/O-bound operations (Ollama calls, GitHub fetches).

Use Records for all DTOs and Data Carriers to ensure immutability.

Favor Constructor Injection over @Autowired.

Use Optional<T> for return types that may be empty; avoid null.
