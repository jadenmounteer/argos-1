import { useVocal } from "../context/VocalContext";
/**
 * Setting buttons: Listening (wake word), TTS (voice on/off), Use local directives (isGrounded).
 */
export function ButtonStrip({
  speakingOn = false,
  setSpeakingOn = () => {},
  isGrounded = false,
  setIsGrounded = () => {},
}: {
  speakingOn?: boolean;
  setSpeakingOn?: (value: boolean) => void;
  isGrounded?: boolean;
  setIsGrounded?: (value: boolean) => void;
} = {}) {
  const {
    listeningMode,
    toggleListeningMode,
    porcupineLoaded,
    wakeWordUnavailableReason,
    isActive,
  } = useVocal();
  const status = !listeningMode
    ? null
    : isActive
      ? "Listening…"
      : porcupineLoaded
        ? 'Say "Argos one"'
        : (wakeWordUnavailableReason ?? "Loading…");
  return (
    <div className="button-strip-wrapper">
      <div className="button-strip">
        <div className="setting-button" onClick={toggleListeningMode}>
          {listeningMode ? "🎙️" : "🔕"}
        </div>

        <div
          className="setting-button"
          onClick={() => setSpeakingOn(!speakingOn)}
        >
          {speakingOn ? "🗣️" : "😶"}
        </div>

        <div
          className={`setting-button setting-button--grounded-toggle ${isGrounded ? "setting-button--on" : ""}`}
          onClick={() => setIsGrounded(!isGrounded)}
          role="switch"
          aria-checked={isGrounded}
          aria-label="Use local directives"
          title="Use local directives"
        >
          📋
        </div>
      </div>
      <label className="button-strip-label">
        <span className="button-strip-label-text">Use local directives</span>
        <span className="button-strip-label-value" aria-live="polite">
          {isGrounded ? "On" : "Off"}
        </span>
      </label>
      {status && (
        <p className="button-strip-status" role="status">
          {status}
        </p>
      )}
    </div>
  );
}
