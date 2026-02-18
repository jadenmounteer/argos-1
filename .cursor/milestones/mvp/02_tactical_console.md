# Milestone 02: Tactical Console (The Interface)

## Goal

Build the React LCARS-inspired UI and integrate the Web Speech API with an "Always-On" Wake Word ("ARGOS-1") for hands-free command execution.

### Architecture Overview

### Tasks

#### [ ] Task 1: LCARS Foundation & Scaffolding

- Initialize React + Vite + TypeScript.
- Install `@starfleet-technology/lcars-react` and Tailwind CSS.
- **Config:** Setup a `tailwind.config.js` with the Starfleet palette:
  - Atomic Tangerine: `#ff9966`
  - Neon Blue: `#6699ff`
  - Pale Violet: `#cc99ff`
- Create the "Bridge" Layout: A fixed-position frame with the iconic LCARS "Sweep" (the large curved border).

#### [ ] Task 2: The "Ears" (Wake Word & Speech)

- **Implement `use-ear` Hook:** Create a `VocalProvider` that listens for the specific keyword "ARGOS" or "ARGOS-1".
- **Logic Switch:** - **Passive:** Listening only for the Wake Word.
  - **Active:** Upon detection, play the LCARS "Chime" and begin full transcription into the command input.
- **Auto-Submit:** Implement a "Silence Detection" timer. If the user stops speaking for 1.5s after the command, automatically trigger the JSON-RPC POST.

#### [ ] Task 3: JSON-RPC over SSE Bridge

- Build an `useIntelligenceStream` hook to connect to `POST /api/v1/command`.
- **Stream Processing:** Use `TextDecoder` to read the SSE chunks and dispatch React state updates for `THOUGHT` vs `RESPONSE` chunks.
- **Acceptance:** The UI must stream tokens word-by-word into the log window.

#### [ ] Task 4: Engineering Log Display

- Create a `TacticalLog` component.
- **Styling:** - Thoughts: Italicized, low-opacity text in the "Diagnostics" panel.
  - Responses: High-contrast bold text in the "Main Viewport."
- **Feedback:** Add a pulsing "Analyzing..." status bar when a `thought` chunk is being processed but no `response` is yet available.

#### [ ] Task 5: Containerized Orchestration

- Update `docker-compose.yml` to include the `argos-console` (React) service.
- **Networking:** Ensure the React frontend can resolve the `argos-kernel` service in the Docker network.
- **Environment:** Pass `VITE_KERNEL_URL=http://argos-kernel:8080` to the build process.
