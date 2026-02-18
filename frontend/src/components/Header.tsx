/**
 * Top-left header: status/icon placeholder + app title (static text, not a button).
 * Title uses LCARS-style display font, yellow/gold, all caps.
 */
export function Header() {
  return (
    <header style={{ display: 'flex', alignItems: 'center', gap: '0.5rem' }}>
      <div data-status aria-hidden style={{ width: 48, height: 24 }} />
      <h1 className="app-title">Argos 1</h1>
    </header>
  )
}
