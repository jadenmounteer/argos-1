import { useVocal } from '../context/VocalContext'

/**
 * Prompt input area below the button strip. Controlled by VocalProvider;
 * read-only during active speech transcription.
 */
export function PromptArea() {
  const { promptText, setPromptText, isActive } = useVocal()
  return (
    <div className="prompt-area">
      <textarea
        id="prompt-input"
        rows={4}
        placeholder="Prompt area"
        aria-label="Prompt area"
        className="prompt-area__input"
        value={promptText}
        onChange={(e) => setPromptText(e.target.value)}
        readOnly={isActive}
      />
    </div>
  )
}
