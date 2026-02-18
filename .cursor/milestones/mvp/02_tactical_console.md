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

#### [x] Task 1: LCARS Foundation & Scaffolding

- Initialize React + Vite + TypeScript.
- Install `@starfleet-technology/lcars-react` and Tailwind CSS.
- **Config:** Setup a `tailwind.config.js` with the Starfleet palette:
  - Atomic Tangerine: `#ff9966`
  - Neon Blue: `#6699ff`
  - Pale Violet: `#cc99ff`
- Create the "Bridge" Layout: A fixed-position frame with the iconic LCARS "Sweep" (the large curved border).

#### [x] Task 2: The "Ears" (Wake Word & Speech)

Goal: Create a VocalProvider that manages the transition from "Always Listening" for the wake word to "Transcribing" the command.

1. Core Engine & Initialization
   Primary Engine: Use @picovoice/porcupine-react.

Config: Initialize usePorcupine using VITE_PICOVOICE_ACCESS_KEY from .env.

Model Path: Load the custom "ARGOS-1" model from public/models/argos-one.ppn (Web WASM version).

Graceful Fallback: If the Access Key is missing or initialization fails, the app shows an unavailable state (no manual mode; wake word only).

2. Logic State Machine
   Passive Mode: The default state. Listening only for "ARGOS One".

Active Mode: Triggered by Porcupine detection.

Action: Execute an onWakeWordDetected callback.

Action: Start window.SpeechRecognition (or polyfill) with continuous: true and interimResults: true.

Action: Fill the UI prompt area in real-time as the user speaks.

Inhibition Mode (The Gatekeeper):

State: A boolean isInhibited (to be controlled by Task 6).

Behavior: While true, ignore all speech results and Porcupine detections. Do not stop the engines; simply drop the data to prevent the AI from hearing itself or the system chimes.

3. Silence Detection & Dispatch
   Timer: Implement a 1.5s silence detection.

Callback Execution: When silence is reached:

Finalize the transcript.

Call onCommandReady(transcript).

Revert to Passive Mode.

Note: Do not strip "Argos One" from the transcript (the Kernel will handle persona parsing).

4. UI Integration
   The Toggle: Map a button in the Tactical Console to toggle "Listening Mode" (Global Ears On/Off).

Visuals: \* Mode ON: Display 🎙️ icon.

Mode OFF: Display 🔕 icon.

The Frame: Set echoCancellation: true in getUserMedia constraints to optimize for open-speaker environments.

5. Technical Contract (For Task 6 Compatibility)
   Export a triggerChime() stub and an inhibit(boolean) function. This allows Task 6 to plug in the actual LCARS sounds and "Mute" logic later without refactoring the transcription flow.Simply set an error state so task 6 can hook into this later.

Developer Note for Cursor
"Keep the VocalProvider decoupled. It should only emit the final string via onCommandReady. Use the Gatekeeper pattern for inhibition: if (isInhibited) return; inside your speech result handlers. Ensure the getUserMedia constraints are passed to the WebVoiceProcessor used by Picovoice."

#### [x] Task 3: JSON-RPC over SSE Bridge

Goal: Implement useIntelligenceStream to connect the LCARS UI to the ARGOS-1 Kernel using a JSON-RPC over Fetch-based SSE stream.

Prioritize the Fetch + ReadableStream pattern. Ensure you distinguish between the event: and data: lines in the SSE stream to correctly route text to either the thoughtLog or mainResponse states. Do not use params.text for the request; use params.input to match the Kernel DTO. Finally, ensure the 5s timeout is only cleared by an actual data event, not just the HTTP 200 response headers.

Phase 1: The POST-Stream Handshake ✅
Endpoint: ${import.meta.env.VITE_KERNEL_URL}/api/v1/command

Method: Must use fetch (POST), not EventSource, to support the JSON-RPC body.

Payload Structure: ```json
{
"jsonrpc": "2.0",
"method": "process_voice",
"params": { "input": "CLEANED_TRANSCRIPT" },
"id": "unique-uuid"
}

Stream Initialization: Use ReadableStream and TextDecoder("utf-8") to process the response body incrementally.

Phase 2: Named Event Parsing & State Routing ✅
The Kernel sends Named Events. Do not just parse raw data; you must route based on the event: line.

Logic:

Identify the event: type (e.g., event: thought or event: response).

Parse the subsequent data: line: JSON.parse(dataLine.replace('data: ', '')).

Extract text from payload.params.text.

State Management:

thoughtLog (Science Teal): Append text from event: thought.

mainResponse (Command Gold): Append text from event: response.

Sentence Buffer (for Task 7):

Monitor mainResponse. Use the heuristic Regex /[.!?](\s+|$)/ to detect full sentences. Speech synthesis (implemented in future task) will only read the response, not the thoughts.

Action: Call onResponseSentence(sentence) as soon as a match is found. "Flush" any remaining text when the stream closes.

Phase 3: Loading state✅
Emit a thought event immediately to let the user know it's working.

Have the main wrapper bars pulse to portray loading state.

#### [ ] Task 5: Containerized Orchestration

- Update `docker-compose.yml` to include the `argos-console` (React) service.
- **Networking:** Ensure the React frontend can resolve the `argos-kernel` service in the Docker network.
- **Environment:** Pass `VITE_KERNEL_URL=http://argos-kernel:8080` to the build process.

#### [x] Task 6: Tactical Audio Engine

Implement an AudioBridge Service: Create a specialized service (using a custom hook or a library like howler.js) to manage three distinct audio channels:

Channel 1 (Ambience): A low-priority, infinite loop of the "Beep Sequences" and "Background Hum."

Channel 2 (System Cues): Instant "one-shot" sounds for INPUT_ACKNOWLEDGED, COMMAND_SUBMITTED (the Enter action), and FAILURE.

Channel 3 (Persona): The COMPUTER_ON and COMPUTER_OFF cues that trigger when the voice-listening mode toggles.

State-Linked Triggers: Link audio events to your application state:

WAKE_WORD_DETECTED ➔ dictation-enabled.mp3

SUBMIT_COMMAND ➔ Play input-sent-to-api.mp3

STREAM_ERROR ➔ Play input-failed.mp3

The above audio files can be found in the assets folder.

#### [ ] Task 7: The "Voice" (Text-to-Speech Synthesis) (This actually is covered in another milestone)

- **Implementation:** Integrate the `window.speechSynthesis` API or a "Majel-cloned" ElevenLabs/Together.ai endpoint.
- **Voice Selection:** - **Option A (Local):** Filter `window.speechSynthesis.getVoices()` for the "Google UK English Female" or "Microsoft Zira" voice (the closest native matches to the TNG computer's cadence).
  - **Option B (Advanced):** Implement a TTS hook that triggers the moment the `response` chunk of the SSE stream is finalized.
- **Persona Rules:** Ensure the voice uses a monotone, rhythmic "Majel" cadence. The voice should _only_ read the `response` content, never the `thought` (diagnostics) content.

Only the response is read. Thoughts are not read.

#### [ ] Task 8 (Totally optional). Ambient audio

Ambient Loop Logic: Ensure the background loop starts only after the first user interaction (to satisfy browser "Auto-play" policies) and fades in smoothly to avoid a jarring start.

When the AI is "Thinking" (Task 4), you can slightly lower the volume of the Ambience Loop and increase the frequency of the Beep Sequences. This psychologically signals to the user that the "Computer" is busy working on their request.

Note even sure I want to do this.
