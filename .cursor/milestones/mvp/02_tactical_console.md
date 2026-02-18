# Milestone 02: Tactical Console (The Interface)

## Goal

Build the React LCARS-inspired UI and integrate the Web Speech API with an "Always-On" Wake Word ("ARGOS-1") for hands-free command execution.

### Design Guidelines

1. The Layout Blueprint (The "Sweep")
   The most defining feature of LCARS is the "Sweep" or "Elbow"—a large, curved border that frames the interface.

The Frame: A thick, rounded border should occupy the top and left sides of the screen.

Sidebars: The left sidebar (approx. 200px wide) should house static ship status indicators and "mode" toggles (e.g., VOICE: ON/OFF).

The Main Viewport: A large black central area where the "Tactical Logs" (your SSE stream) will scroll.

2. Official Color PaletteLCARS uses a highly specific set of "24th-century" pastel and vibrant tones. Avoid standard bright primary colors. Use these Tailwind-ready hex codes:RoleColor NameHex CodeTailwind Class (Custom)Tactical/AlertTactical Orange#FF6600bg-argos-orangeCommandCommand Gold#FFD700bg-argos-goldScienceScience Teal#008080bg-argos-tealEngineeringEngineering Red#FF0000bg-argos-redFunctionPale Violet#CC99CCbg-argos-purpleBackgroundSpace Black#000000bg-black

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
- **Logic Switch:** - **Passive:** Listening only for the Wake phrase: "ARGOS One".
  - **Active:** Upon detection, play the LCARS "Chime" (will be implemented in task 6) and begin full transcription into the prompt area.
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

#### [ ] Task 6: Tactical Audio Engine

Implement an AudioBridge Service: Create a specialized service (using a custom hook or a library like howler.js) to manage three distinct audio channels:

Channel 1 (Ambience): A low-priority, infinite loop of the "Beep Sequences" and "Background Hum."

Channel 2 (System Cues): Instant "one-shot" sounds for INPUT_ACKNOWLEDGED, COMMAND_SUBMITTED (the Enter action), and FAILURE.

Channel 3 (Persona): The COMPUTER_ON and COMPUTER_OFF cues that trigger when the voice-listening mode toggles.

State-Linked Triggers: Link audio events to your application state:

WAKE_WORD_DETECTED ➔ Play Input_Acknowledged_Chirp.

SUBMIT_COMMAND ➔ Play Enter_Action_Beep.

STREAM_ERROR ➔ Play System_Failure_Tone.

Ambient Loop Logic: Ensure the background loop starts only after the first user interaction (to satisfy browser "Auto-play" policies) and fades in smoothly to avoid a jarring start.

When the AI is "Thinking" (Task 4), you can slightly lower the volume of the Ambience Loop and increase the frequency of the Beep Sequences. This psychologically signals to the user that the "Computer" is busy working on their request.

I have audio sounds I'll add to the assets directory soon.

#### [ ] Task 7: The "Voice" (Text-to-Speech Synthesis)

- **Implementation:** Integrate the `window.speechSynthesis` API or a "Majel-cloned" ElevenLabs/Together.ai endpoint.
- **Voice Selection:** - **Option A (Local):** Filter `window.speechSynthesis.getVoices()` for the "Google UK English Female" or "Microsoft Zira" voice (the closest native matches to the TNG computer's cadence).
  - **Option B (Advanced):** Implement a TTS hook that triggers the moment the `response` chunk of the SSE stream is finalized.
- **Persona Rules:** Ensure the voice uses a monotone, rhythmic "Majel" cadence. The voice should _only_ read the `response` content, never the `thought` (diagnostics) content.
