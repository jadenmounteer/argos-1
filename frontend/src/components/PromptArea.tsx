/**
 * Prompt input area below the button strip. Skeleton: placeholder only.
 */
export function PromptArea() {
  return (
    <div className="prompt-area">
      <textarea
        id="prompt-input"
        rows={4}
        placeholder="Prompt area"
        aria-label="Prompt area"
        className="prompt-area__input"
      />
    </div>
  )
}
