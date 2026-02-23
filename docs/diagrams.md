# ARGOS-1 diagrams

High-level process and backend architecture in Mermaid.

---

## High-level: frontend–backend interaction (one command, SSE stream)

```mermaid
sequenceDiagram
  participant User
  participant Frontend as Frontend_UI_Hooks
  participant Kernel as Kernel_Backend
  participant Ollama as Ollama_LLM

  User->>Frontend: Submit command (voice or text)
  Frontend->>Frontend: sendCommand(text)
  Frontend->>Kernel: POST /api/v1/command (JSON-RPC process_voice)
  Kernel->>Kernel: CommandGateway.runStream
  Kernel->>Kernel: SystemPromptProvider.getSystemPrompt
  Kernel->>Kernel: SseEmitter created, thread started
  Kernel->>Kernel: LlmStreamPort.stream(input, systemPrompt, handler)
  Kernel->>Ollama: StreamingChatLanguageModel.generate(messages)
  Ollama-->>Kernel: Token stream
  Kernel->>Kernel: TokenHandler.onToken (parse thought vs response)
  Kernel-->>Frontend: SSE event: thought (data.params.text)
  Kernel-->>Frontend: SSE event: response (data.params.text)
  Frontend->>Frontend: setThoughtLog / setMainResponse, optional TTS
  Ollama-->>Kernel: onComplete
  Kernel-->>Frontend: SSE event: result (thought, response)
  Kernel-->>Frontend: emitter.complete()
  Frontend->>Frontend: onResponseFinalized, setIsStreaming(false)
```

- Frontend POSTs JSON-RPC to `/api/v1/command` and consumes SSE; backend parses `<thought>` and response, streams events to the client.

---

## Backend architecture and data flow (command stream to Ollama and SSE)

```mermaid
flowchart TD
  subgraph REST [REST Adapter]
    KC[KernelController]
  end

  subgraph App [Application Layer]
    CG[CommandGateway]
    JSR[JsonRpcCommandRequest]
    TH[TokenHandler parse thought vs response]
  end

  subgraph Domain [Domain Ports]
    LSP[LlmStreamPort]
    SPP[SystemPromptProvider]
  end

  subgraph Infra [Infrastructure]
    OSA[OllamaStreamingAdapter]
    ASP[ArgosSystemPromptProvider]
  end

  Ext[Ollama LLM]

  KC -->|runStream| CG
  CG -->|getSystemPrompt| SPP
  SPP --> ASP
  CG -->|stream input, systemPrompt, handler| LSP
  LSP --> OSA
  OSA -->|generate messages, StreamingResponseHandler| Ext
  Ext -->|token stream| OSA
  OSA -->|onToken / onComplete / onError| TH
  TH -->|SSE thought/response/result| Emitter[SseEmitter]
  Emitter -->|HTTP chunked| Client[Client]
```

- CommandGateway orchestrates LlmStreamPort and SystemPromptProvider; OllamaStreamingAdapter calls Ollama via LangChain4j.

## Agentic AI workflow

flowchart TD
subgraph REST [REST Adapter]
KC[KernelController]
end

subgraph App [Application Layer]
CG[CommandGateway]
IR[Intent & Voice Command Router]
TH[TokenHandler]
CRD[ChatRequestDTO - isGrounded flag]
end

subgraph Domain [Domain Ports]
LSP[LlmStreamPort]
SPP[SystemPromptProvider]
DSP[DirectivePort]
GRP[GitRepoPort]
end

subgraph Infra [Infrastructure]
OSA[OllamaStreamingAdapter]
ASP[ArgosSystemPromptProvider]
DSS[DirectiveScannerService - Local FS]
GHS[GitHubService - Remote API]
DSN[DiffSanitizer - Utility]
end

Ext[Ollama LLM]
GAPI[GitHub API]

%% Request Flow
KC -->|ChatRequestDTO| CG
CG --> IR

%% Command Detection & Hydration
IR -->|Regex: 'Review PR'| GRP
GRP --> GHS
GHS -.-> DSN
GHS --- GAPI

%% Grounding Logic
IR -->|if Toggle ON or PR found| DSP
DSP --> DSS

%% Intelligence Loop
CG -->|Aggregated Context| SPP
SPP --> ASP
CG -->|Grounded Prompt| LSP
LSP --> OSA
OSA -->|Stream| Ext

%% Output
OSA --> TH
TH --> Emitter[SseEmitter]
