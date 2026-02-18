# Vocal Feedback

## Goal

Integrate the Speech Synthesis API so ARGOS can respond verbally with a Starfleet-style personality.

### Tasks

Note on User Gesture: Most modern browsers block speechSynthesis until the user interacts with the page (e.g., clicks a button). Ensure the "Start Bridge" button in the UI handles this permission handshake.

Note on Performance: Speech synthesis is CPU-light, but "Speech Recognition" is heavy. By muting the mic during playback (Task 4), you save significant local processing power for the LLM.

[ ] Task 1: Speech Synthesis Integration
Implement the window.speechSynthesis API within the React Tactical Console.

Create a useArgosSpeaker hook to manage the queue of messages to be spoken.

Implement the Auditory Feedback Gateway: Treat the speaker as a 'Secondary Display' that subscribes to the Kernel's output stream, separate from the visual log.

Senior Note: Ensure that if a new command is issued while ARGOS is speaking, the current speech is cancelled (speechSynthesis.cancel()) to maintain responsiveness.

[ ] Task 2: Personality & Voice Selection
Filter available system voices to find the most "Computer-like" option (e.g., "Google UK English Female" or "Microsoft Zira").

Adjust pitch, rate, and volume parameters to match the clinical, efficient tone of a ship's computer.

[ ] Task 3: Audio "Chirps" & Sound Effects
Integrate classic low-frequency "chirps" or "acknowledge" sounds for specific system events:

Wake Word Detected: Short, high-pitched chirp.

Processing Started: Low-frequency rhythmic hum.

Review Complete: Neutral double-beep.

Use the Web Audio API or simple HTML5 Audio tags to trigger these locally.

[ ] Task 4: The "Mute" Circuit (Echo Cancellation)
Critical Logic: Implement a "Mic Lock" that automatically disables the SpeechRecognition listener while speechSynthesis is active.

This prevents ARGOS-1 from "hearing" its own voice and attempting to process its own responses.

[ ] Task 5: Verbal State Updates
Add "Ambient Verbalization." If a GitHub fetch takes longer than 3 seconds, ARGOS should say: "Accessing subspace link. Please stand by."

This provides auditory confirmation that the system hasn't frozen.
