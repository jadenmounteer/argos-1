import { useVocal } from '../context/VocalContext'

/**
 * Prompt input area below the button strip. Controlled by VocalProvider;
 * read-only during active speech transcription.
 * When isGrounded is true, a blue glow is applied (directives mode).
 */
export function PromptArea({ isGrounded = false }: { isGrounded?: boolean }) {
  const { promptText, setPromptText, isActive } = useVocal()
  const inputClass = isGrounded
    ? 'prompt-area__input prompt-area__input--grounded'
    : 'prompt-area__input'
  return (
    <div className="prompt-area">
      <textarea
        id="prompt-input"
        rows={4}
        placeholder="Prompt area"
        aria-label="Prompt area"
        className={inputClass}
        value={promptText}
        onChange={(e) => setPromptText(e.target.value)}
        readOnly={isActive}
      />
    </div>
  )
}
