/**
 * Large logs and responses display (right column). Read-only; users can copy text.
 * Thought streams and AI response populate here (Task 4 will add TacticalLog styling).
 */
export function LogsView({
  thoughtLog = '',
  mainResponse = '',
  isStreaming = false,
}: {
  thoughtLog?: string
  mainResponse?: string
  isStreaming?: boolean
}) {
  const showPlaceholderThought = isStreaming && !thoughtLog
  return (
    <div className="logs-view" role="log" aria-label="Logs and responses">
      {thoughtLog ? (
        <section className="logs-view__thoughts" aria-label="Thoughts">
          <pre className="logs-view__thought-text">{thoughtLog}</pre>
        </section>
      ) : showPlaceholderThought ? (
        <section className="logs-view__thoughts" aria-label="Thoughts">
          <pre className="logs-view__thought-text">Analyzing...</pre>
        </section>
      ) : null}
      {mainResponse ? (
        <section className="logs-view__response" aria-label="Response">
          <pre className="logs-view__response-text">{mainResponse}</pre>
        </section>
      ) : null}
      {!thoughtLog && !mainResponse && !showPlaceholderThought ? (
        <p className="logs-view__empty">System Logs</p>
      ) : null}
    </div>
  )
}
