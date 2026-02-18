# MCP Bridge

## Goal

Expose the backend as an MCP Server so the same ARGOS "Brain" can be utilized directly inside the Cursor Chat.

Acknowledged, Commander. We are initiating the final frontier: Milestone 07: Multi-Surface Sync (The MCP Bridge).

This milestone is the "Senior Architect" masterstroke. By implementing the Model Context Protocol (MCP), you transform ARGOS-1 from a standalone app into a universal intelligence provider. This allows the same "Brain" and "Directives" you built to be plugged directly into Cursor, Claude Desktop, or any other MCP-compliant IDE.

Add this to your .cursor/milestones/07_mcp_bridge.md file.

Milestone 07: Multi-Surface Sync (The MCP Bridge)
Goal: Expose the ARGOS-1 Kernel as an MCP Server, allowing the Agent's tools and directives to be utilized natively inside Cursor and other AI-powered IDEs.

### Tasks

Note on Transport: Most MCP clients use stdio (Standard Input/Output). Ensure your Spring Boot logs are directed to a file (Logback) and not to the console, otherwise, your log messages will corrupt the MCP protocol stream.

[ ] Task 1: MCP Protocol Implementation
Integrate an MCP Java SDK (or implement the JSON-RPC spec) into the Spring Boot Kernel.

Map the existing Agentic Tools (from Milestone 06) to MCP "Tool" definitions.

Acceptance: The Kernel can now respond to list_tools and call_tool requests over Standard Input/Output (stdio) or HTTP.

[ ] Task 2: Directive Resource Mapping
Expose the .argos/directives folder as MCP Resources.

This allows an external IDE (like Cursor) to "read" your architectural standards directly from the ARGOS-1 server.

[ ] Task 3: Cursor Connection (The "Subspace Link")
Configure the Cursor features settings to recognize your local Spring Boot app as an MCP Server.

Senior Note: This effectively makes ARGOS-1 the "Backend" for Cursor's "Frontend," unifying your rules across both interfaces.

[ ] Task 4: Cross-Surface Context Sharing
Implement a shared state mechanism so that if ARGOS-1 learns something via the Tactical Console (Milestone 02), that context is available when you chat with it inside Cursor.

This creates a "Unified Intelligence" experience.

[ ] Task 5: Security & Boundary Enforcement
Implement a "Consent Gate" for the MCP server. Ensure that external tools can only access files and tools explicitly permitted by the ARGOS-1 Kernel configuration.
