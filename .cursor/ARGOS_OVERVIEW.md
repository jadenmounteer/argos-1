# ARGOS-1: System Overview

## 1. The Vision

ARGOS-1 (Architectural Review & Governance Orchestration System) is a sovereign, voice-activated AI Agent designed to serve as an "Automated Lead Engineer." It bridges the gap between static code analysis and human-level architectural reasoning.

## 2. Why "ARGOS"?

In Greek mythology, Argos Panoptes was the "All-Seeing" watchman with one hundred eyes. Like its namesake, ARGOS-1 monitors the codebase from every angle—security, structural integrity, and logic—ensuring that no "architectural drift" occurs during high-velocity development.

## 3. Why it is "Agentic" (and not just RAG)

Most AI tools are reactive; ARGOS-1 is proactive.

Reasoning over Retrieval: While standard AI (RAG) simply searches for answers, ARGOS-1 uses a ReAct (Reasoning + Action) loop. It formulates a plan, selects the necessary tools (GitHub API, File System, Linter), and executes them autonomously to verify its own hypotheses.

Tool-Use: ARGOS-1 doesn't just "talk" about code; it "interacts" with the environment to fetch PR diffs and validate them against internal Directives.

## 4. Architecture: Domain-Driven Orchestration

To ensure infinite scalability without increasing complexity, ARGOS-1 is built on a Domain-Driven Micro-Kernel architecture:

The Command Hub (Orchestrator): A Spring Boot "Central Brain" that handles intent classification and voice processing.

Pluggable Domains: Logic is encapsulated into specific domain services.

domain-engineering: Specialized in PR reviews and Java/Spring patterns.

domain-content: (Future) Specialized in documentation and technical writing.

domain-security: (Future) Specialized in vulnerability scanning.

The Unified Interface: A React-based "Tactical Console" that remains agnostic of the backend's complexity, routing requests to the appropriate domain expert.

## 5. Core Technical Pillar: The "Directives"

The system's "Source of Truth" is a shared .argos/directives folder. This allows ARGOS-1 to remain grounded in the specific, opinionated standards of the engineering team, eliminating generic AI "hallucinations" and replacing them with project-specific governance.
