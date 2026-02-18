/**
 * Tactical Audio Engine (Task 6): one-shot sounds for state-linked triggers.
 * Channel 1 (Ambience) infinite loop is deferred to Task 8.
 * Assets are served from public/assets/ (paths under /assets/).
 */
const ASSETS = "/assets/audio";
const VOLUME = 0.15;

function playOneShot(path: string): void {
  if (typeof window === "undefined") return;
  const audio = new Audio(path);
  audio.volume = VOLUME;
  const p = audio.play();
  if (p != null && typeof p.catch === "function") {
    p.catch((error) => {
      // Missing file or autoplay blocked; fail silently
      console.error(`Failed to play audio file: ${path}`, error);
    });
  }
}

/** WAKE_WORD_DETECTED: play when user says the wake word. */
export function playWakeWordDetected(): void {
  playOneShot(`${ASSETS}/dictation-enabled.mp3`);
}

/** SUBMIT_COMMAND: play when command is sent to the API. */
export function playCommandSubmitted(): void {
  playOneShot(`${ASSETS}/input-sent-to-api.mp3`);
}

/** STREAM_ERROR: play when the stream or request fails. */
export function playStreamError(): void {
  playOneShot(`${ASSETS}/input-failed.mp3`);
}

/** Channel 3 (Persona): play when listening mode turns on. */
export function playComputerOn(): void {
  playOneShot(`${ASSETS}/system-activate.mp3`);
}

/** Channel 3 (Persona): play when listening mode turns off. */
export function playComputerOff(): void {
  //   playOneShot(`${ASSETS}/system-off.mp3`); // Need to interact with the computer first for this to work.
}
