# Agentic flow

flowchart TD
%% Actors and Interfaces
User((Engineer)) -->|Voice Command| UI[Tactical Console]
UI -->|Transcription| Hub[Spring Boot Hub]

    %% The Orchestration Kernel
    subgraph Kernel [Nervous System]
        Hub -->|Context + Prompt| LLM{Ollama: DeepSeek-R1}
        LLM -->|Thought Stream| Hub
    end

    %% The Agentic ReAct Loop
    subgraph AgenticLoop [The Reasoning Loop]
        LLM -.->|1. Reason| Thought[Internal Monologue: Thought Tag]
        Thought -->|2. Plan| Action{Decision: Need Action?}

        Action -->|Yes: Tool Call| Tools[Domain Tools]
        Action -->|No: Final Answer| Response[Formulate Response]

        %% Tool Execution
        subgraph Domains [Domain-Driven Experts]
            Tools --> Engineering[Engineering: Fetch PR/Diff]
            Tools --> Sentinel[Sentinel: Scan Directives]
            Tools --> Security[Security: Vulnerability Check]
        end

        Engineering & Sentinel & Security -->|Observation| Thought
    end

    %% Feedback Loop to User
    Response -->|Audio Feedback| TTS[Vocal Interface]
    Response -->|Visual Update| Logs[Tactical Logs]
    TTS & Logs --> User

    %% Styling
    style Kernel fill:#1a1a2e,stroke:#16213e,color:#fff
    style AgenticLoop fill:#16213e,stroke:#0f3460,color:#fff
    style LLM fill:#e94560,stroke:#fff,color:#fff
    style Domains fill:#0f3460,stroke:#533483,color:#fff
